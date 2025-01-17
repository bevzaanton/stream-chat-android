package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.example.databinding.ActivityMainBinding;

import static com.getstream.sdk.chat.enums.Filters.and;
import static com.getstream.sdk.chat.enums.Filters.eq;


/**
 * This activity shows a list of channels
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";
    final String USER_ID = "bender";
    // User token is typically provided by your server when the user authenticates
    final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ";
    private ChannelListViewModel viewModel;

    // establish a websocket connection to stream
    protected Client configureStreamClient() {
        Client client = StreamChat.getInstance(getApplication());
        //client.enableOfflineStorage();

        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Bender");
        extraData.put("image", "https://bit.ly/321RmWb");
        User user = new User(USER_ID, extraData);
        client.setUser(user, USER_TOKEN);
        return client;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);

        // setup the client
        Client client = configureStreamClient();

        // we're using data binding in this example
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this);

        // most the business logic for chat is handled in the ChannelListViewModel view model
        viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        // just get all channels
        FilterObject filter = and(eq("type", "messaging"));

        // ChannelViewHolderFactory factory = new ChannelViewHolderFactory();
        //binding.channelList.setViewHolderFactory(factory);
        viewModel.setChannelFilter(filter);
        // set the viewModel data for the activity_main.xml layout
        binding.setViewModel(viewModel);

        binding.channelList.setViewModel(viewModel, this);



        // setup an onclick listener to capture clicks to the user profile or channel
        MainActivity parent = this;
        binding.channelList.setOnChannelClickListener(channel -> {
            // open the channel activity
            Intent intent = new Intent(parent, ChannelActivity.class);
            intent.putExtra(EXTRA_CHANNEL_TYPE, channel.getType());
            intent.putExtra(EXTRA_CHANNEL_ID, channel.getId());
            startActivity(intent);
        });
        binding.channelList.setOnUserClickListener(user -> {
            // open your user profile
        });
        binding.ivAdd.setOnClickListener(this::createNewChannelDialog);
    }

    void createNewChannelDialog(View view) {

        final EditText inputName = new EditText(this);
        inputName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        inputName.setHint("Type a channel name");
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Create a Channel")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.setView(inputName);
        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String channelName = inputName.getText().toString();
                if (TextUtils.isEmpty(channelName)) {
                    inputName.setError("Invalid Name!");
                    return;
                }
                createNewChannel(channelName);
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    void createNewChannel(String channelName){
        Client client = configureStreamClient();

        String channelId = StringUtility.getSaltString(channelName);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", channelName);
        Channel channel = new Channel(client, ModelType.channel_messaging, channelId, extraData);

        Map<String, Object> data = new HashMap<>();
        data.put("name", channelName);

        List<String> members = new ArrayList<>();
        members.add(client.getUser().getId());

        data.put("members", members);

        ChannelQueryRequest request = new ChannelQueryRequest().withData(data).withMessages(10);
        viewModel.setLoading();
        channel.query(request, new QueryChannelCallback() {
            @Override
            public void onSuccess(ChannelState response) {
                Channel channel = response.getChannel();
                channel.setClient(client);
                Intent intent = new Intent(MainActivity.this, ChannelActivity.class);
                intent.putExtra(EXTRA_CHANNEL_TYPE, channel.getType());
                intent.putExtra(EXTRA_CHANNEL_ID, channel.getId());
                startActivity(intent);
                viewModel.addChannels(Arrays.asList(response));
                viewModel.setLoadingDone();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                viewModel.setLoadingDone();
                Toast.makeText(MainActivity.this, errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
