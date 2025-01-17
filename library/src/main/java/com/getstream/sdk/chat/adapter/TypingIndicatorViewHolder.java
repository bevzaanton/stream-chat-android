package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.AvatarGroupView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class TypingIndicatorViewHolder extends BaseMessageListItemViewHolder {
    private MessageListViewStyle style;
    // Tying
    private ImageView iv_typing_indicator;
    private LinearLayout ll_typingusers;

    public TypingIndicatorViewHolder(int resId, ViewGroup viewGroup, MessageListViewStyle s) {
        this(resId, viewGroup);
        style = s;
    }

    public TypingIndicatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);
        iv_typing_indicator = itemView.findViewById(R.id.iv_typing_indicator);
        ll_typingusers = itemView.findViewById(R.id.ll_typing_indicator);
    }

    @Override
    public void bind(Context context,
                     ChannelState channelState,
                     MessageListItem messageListItem,
                     int position, boolean isThread,
                     MessageListView.MessageClickListener l1,
                     MessageListView.MessageLongClickListener messageLongClickListener,
                     MessageListView.AttachmentClickListener l2,
                     MessageListView.UserClickListener userClickListener,
                     MessageListView.ReadStateClickListener readStateClickListener) {

        ll_typingusers.setVisibility(View.VISIBLE);
        iv_typing_indicator.setVisibility(View.VISIBLE);
        ll_typingusers.removeAllViews();

        int i = 0;

        for (User user : messageListItem.getUsers()) {
            AvatarGroupView<MessageListViewStyle> avatarView = new AvatarGroupView<>(context);
            avatarView.setUser(user, style);
            int height = style.getAvatarHeight();
            int width = style.getAvatarWidth();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            if (i == 0)
                params.setMargins(0, 0, 0, 0);
            else
                params.setMargins(- width/2, 0, 0, 0);


            avatarView.setLayoutParams(params);
            ll_typingusers.addView(avatarView);
            i += 1;
        }
        Glide.with(context).asGif().load(R.raw.stream_typing).into(iv_typing_indicator);
    }

    @Override
    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }

}
