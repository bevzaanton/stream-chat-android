package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.LifecycleHandler;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.StreamLifecycleObserver;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class ChannelListViewModel extends AndroidViewModel implements LifecycleHandler {
    private final String TAG = ChannelListViewModel.class.getSimpleName();

    private LazyQueryChannelLiveData<List<Channel>> channels;
    private MutableLiveData<Boolean> loading;
    private MutableLiveData<Boolean> loadingMore;

    private FilterObject filter;
    private QuerySort sort;

    private boolean reachedEndOfPagination;
    private AtomicBoolean initialized;
    private AtomicBoolean isLoading;
    private AtomicBoolean isLoadingMore;
    private boolean queryChannelDone;
    private int pageSize;
    private int subscriptionId = 0;
    private int recoverySubscriptionId = 0;
    private StreamLifecycleObserver lifecycleObserver;
    private Handler retryLooper;

    public ChannelListViewModel(@NonNull Application application) {
        super(application);

        isLoading = new AtomicBoolean(false);
        isLoadingMore = new AtomicBoolean(false);
        initialized = new AtomicBoolean(false);

        reachedEndOfPagination = false;
        pageSize = 25;

        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);

        channels = new LazyQueryChannelLiveData<>();
        channels.viewModel = this;
        sort = new QuerySort().desc("last_message_at");

        setupConnectionRecovery();
        initEventHandlers();

        lifecycleObserver = new StreamLifecycleObserver(this);

        retryLooper = new Handler();
    }

    public LiveData<List<Channel>> getChannels() {
        return channels;
    }

    private void setChannels(List<ChannelState> newChannelsState) {


        // - offline loads first
        // - after that we query the API and load more channels
        // - it's possible that the offline results no longer match the query (so we should remove them)

        List<Channel> newChannels = new ArrayList<>();
        for (ChannelState chan : newChannelsState) {
            newChannels.add(chan.getChannel());
        }
        channels.postValue(newChannels);
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getLoadingMore() {
        return loadingMore;
    }

    public void setChannelsPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (subscriptionId != 0) {
            client().removeEventHandler(subscriptionId);
        }
        if (recoverySubscriptionId != 0) {
            client().removeEventHandler(recoverySubscriptionId);
        }
    }

    public boolean setLoading() {
        if (isLoading.compareAndSet(false, true)) {
            loading.postValue(true);
            return true;
        }
        return false;
    }

    public void setLoadingDone() {
        if (isLoading.compareAndSet(true, false))
            loading.postValue(false);
    }

    private boolean setLoadingMore() {
        if (isLoadingMore.compareAndSet(false, true)) {
            loadingMore.postValue(true);
            return true;
        }
        return false;
    }

    private void setLoadingMoreDone() {
        if (isLoadingMore.compareAndSet(true, false))
            loadingMore.postValue(false);
    }

    public void setChannelFilter(FilterObject filter) {
        if (initialized.get()) {
            Log.e(TAG, "setChannelFilter on an already initialized channel list is a no-op, make sure to set filters *before* consuming channels or create a new ChannelListViewModel if you need a different query");
            return;
        }
        this.filter = filter;
    }

    public void setChannelSort(QuerySort sort) {
        this.sort = sort;
    }

    public Client client() {
        return StreamChat.getInstance(getApplication());
    }

    @Override
    public void resume() {
        setLoading();
    }

    @Override
    public void stopped() {
    }

    private void setupConnectionRecovery() {
        recoverySubscriptionId = client().addEventHandler(new ChatEventHandler() {
            @Override
            public void onConnectionRecovered(Event event) {
                Log.i(TAG, "onConnectionRecovered");
                if (!queryChannelDone) {
                    queryChannelsInner(0);
                    return;
                }
                setLoadingDone();
                boolean changed = false;
                List<Channel> channelCopy = channels.getValue();
                for (Channel channel : client().getActiveChannels()) {
                    int idx = -1;
                    if (channelCopy != null) {
                        idx = channelCopy.lastIndexOf(channel);
                    }
                    if (idx != -1) {
                        channelCopy.set(idx, channel);
                        changed = true;
                    }
                }
                if (changed) channels.postValue(channelCopy);
            }
        });
    }

    private void initEventHandlers() {
        subscriptionId = client().addEventHandler(new ChatEventHandler() {
            @Override
            public void onConnectionChanged(Event event) {
                if (!event.getOnline()) {
                    retryLooper.removeCallbacksAndMessages(null);
                }
            }

            @Override
            public void onNotificationMessageNew(Channel channel, Event event) {
                Message lastMessage = channel.getChannelState().getLastMessage();
                Log.i(TAG, "onMessageNew Event: Received a new message with text: " + event.getMessage().getText());
                Log.i(TAG, "onMessageNew State: Last message is: " + lastMessage.getText());
                Log.i(TAG, "onMessageNew Unread Count " + channel.getChannelState().getCurrentUserUnreadMessageCount());
                upsertChannel(channel);
            }

            @Override
            public void onMessageNew(Channel channel, Event event) {
                Message lastMessage = channel.getChannelState().getLastMessage();
                Log.i(TAG, "onMessageNew Event: Received a new message with text: " + event.getMessage().getText());
                Log.i(TAG, "onMessageNew State: Last message is: " + lastMessage.getText());
                Log.i(TAG, "onMessageNew Unread Count " + channel.getChannelState().getCurrentUserUnreadMessageCount());
                updateChannel(channel, true);
            }

            @Override
            public void onChannelDeleted(Channel channel, Event event) {
                deleteChannel(channel);
            }

            @Override
            public void onChannelUpdated(Channel channel, Event event) {
                updateChannel(channel, false);
            }

            @Override
            public void onMessageRead(Channel channel, Event event) {
                updateChannel(channel, false);
            }
        });
    }

    private boolean updateChannel(Channel channel, boolean moveToTop) {
        List<Channel> channelCopy = channels.getValue();
        int idx = channelCopy.lastIndexOf(channel);

        if (idx != -1) {
            channelCopy.remove(channel);
            channelCopy.add(moveToTop ? 0 : idx, channel);
            channels.postValue(channelCopy);
        }

        return idx != -1;
    }

    private void upsertChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        Boolean removed = channelCopy.remove(channel);
        channelCopy.add(0, channel);
        channels.postValue(channelCopy);
    }

    private boolean deleteChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        Boolean removed = channelCopy.remove(channel);
        channels.postValue(channelCopy);
        return removed;
    }

    public void addChannels(List<ChannelState> newChannelsState) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }

        // - offline loads first
        // - after that we query the API and load more channels
        // - it's possible that the offline results no longer match the query (so we should remove them)

        List<Channel> newChannels = new ArrayList<>();
        for (ChannelState chan : newChannelsState) {
            newChannels.add(chan.getChannel());
        }
        channelCopy.addAll(newChannels);
        channels.postValue(channelCopy);
    }

    private void queryChannelsInner(int attempt) {


        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(pageSize)
                .withMessageLimit(20);

        QueryChannelListCallback queryCallback = new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                queryChannelDone = true;
                setLoadingDone();

                Log.i(TAG, "onSuccess for loading the channels");
                // remove the offline channels before adding the new ones
                setChannels(response.getChannelStates());

                if (response.getChannelStates().size() < pageSize) {
                    Log.i(TAG, "reached end of pagination");
                    reachedEndOfPagination = true;
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels " + errMsg);
                if (attempt > 100) {
                    Log.e(TAG, "tried more than 100 times, give up now");
                    return;
                }
                if (!client().isConnected()) {
                    return;
                }
                int sleep = Math.min(500 * (attempt * attempt + 1), 30000);
                Log.d(TAG, "retrying in " + sleep);
                retryLooper.postDelayed(() -> {
                    queryChannelsInner(attempt + 1);
                }, sleep);
            }
        };
        client().queryChannels(request, queryCallback);
    }

    private void queryChannels() {
        Log.i(TAG, "queryChannels for loading the channels");
        if (!setLoading()) return;
        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(pageSize)
                .withMessageLimit(20);
        client().storage().selectChannelStates(request.query().getId(), 100, new Storage.OnQueryListener<List<ChannelState>>() {
            @Override
            public void onSuccess(List<ChannelState> channels) {
                Log.i(TAG, "Read from local cache...");
                if (channels != null) {
                    addChannels(channels);
                }
            }

            @Override
            public void onFailure(Exception e) {
                // TODO
            }
        });
        queryChannelsInner(0);
    }

    public void loadMore() {
        if (!client().isConnected()) {
            return;
        }

        if (isLoading.get()) {
            Log.i(TAG, "already loading, skip loading more");
            return;
        }
        if (reachedEndOfPagination) {
            Log.i(TAG, "already reached end of pagination, skip loading more");
            return;
        }
        if (!setLoadingMore()) {
            Log.i(TAG, "already loading next page, skip loading more");
            return;
        }

        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(pageSize)
                .withMessageLimit(20);

        if (channels.getValue() != null) {
            request = request.withOffset(channels.getValue().size());
        }

        client().queryChannels(request, new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                Log.i(TAG, "onSuccess for loading more channels");
                setLoadingMoreDone();
                addChannels(response.getChannelStates());

                if (response.getChannelStates().size() < pageSize) {
                    Log.i(TAG, "reached end of pagination");
                    reachedEndOfPagination = true;
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels" + errMsg);
                setLoadingMoreDone();
            }
        });
    }

    class LazyQueryChannelLiveData<T> extends MutableLiveData<T> {
        protected ChannelListViewModel viewModel;

        @Override
        protected void onActive() {
            super.onActive();
            if (viewModel.initialized.compareAndSet(false, true)) {
                viewModel.queryChannels();
            }
        }

    }

}