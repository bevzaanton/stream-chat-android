package com.getstream.sdk.chat.rest.request;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.google.gson.annotations.SerializedName;

public class QueryChannelsRequest extends BaseQueryChannelRequest<QueryChannelsRequest> {

    @SerializedName("filter_conditions")
    private FilterObject filter;

    @SerializedName("sort")
    private QuerySort sort;

    @SerializedName("message_limit")
    private Number messageLimit;

    @SerializedName("limit")
    private Number limit;

    @SerializedName("offset")
    private Number offset;

    public QueryChannelsRequest() {
        this(new FilterObject(), new QuerySort());
    }

    public QueryChannelsRequest(@NonNull FilterObject filter,@NonNull QuerySort sort) {
        this.filter = filter;
        this.watch = true;
        this.state = true;

        this.sort = sort.clone();
    }

    public QueryChannelsQ query() {
        return new QueryChannelsQ(filter, sort);
    }

    protected QueryChannelsRequest cloneOpts() {
        QueryChannelsRequest _this = new QueryChannelsRequest(this.filter, this.sort);
        _this.state = this.state;
        _this.watch = this.watch;
        _this.limit = this.limit;
        _this.offset = this.offset;
        _this.presence = this.presence;
        _this.messageLimit = this.messageLimit;
        return _this;
    }

    public QueryChannelsRequest withMessageLimit(int limit) {
        QueryChannelsRequest clone = this.cloneOpts();
        clone.messageLimit = limit;
        return clone;
    }

    public QueryChannelsRequest withLimit(int limit) {
        QueryChannelsRequest clone = this.cloneOpts();
        clone.limit = limit;
        return clone;
    }

    public QueryChannelsRequest withOffset(int offset) {
        QueryChannelsRequest clone = this.cloneOpts();
        clone.offset = offset;
        return clone;
    }
}
