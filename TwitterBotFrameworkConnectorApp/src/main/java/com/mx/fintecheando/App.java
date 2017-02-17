package com.mx.fintecheando;

/*
 * #%L
 * TwitterBotFrameworkConnectorApp
 * %%
 * Copyright (C) 2012 - 2017 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.IOException;
import java.util.Map;

//import org.nanohttpd.NanoHTTPD;
// NOTE: If you're using NanoHTTPD < 3.0.0 the namespace is different,
//       instead of the above import use the following:
import fi.iki.elonen.NanoHTTPD;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ConversationsApi;
import io.swagger.client.model.Activity;
import io.swagger.client.model.ActivitySet;
import io.swagger.client.model.ChannelAccount;
import io.swagger.client.model.Conversation;
import io.swagger.client.model.ResourceResponse;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;

public class App extends NanoHTTPD {

    private static String user_message = "";

    private static String user_name = "@";

    public App() throws IOException {
        super(28080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:28080/ \n");
    }

    public static void main(String[] args) throws TwitterException, ApiException, InterruptedException {
        try {
            new App();
            TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
            twitterStream.addListener(listener);
            // user() method internally creates a thread which manipulates
            // TwitterStream and calls these adequate listener methods
            // continuously.
            twitterStream.user();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    private static final UserStreamListener listener = new UserStreamListener() {

        @Override
        public void onStatus(Status status) {
            System.out.println("onStatus @" + status.getUser().getScreenName() + " - " + status.getText());
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
        }

        @Override
        public void onDeletionNotice(long directMessageId, long userId) {
            System.out.println("Got a direct message deletion notice id:" + directMessageId);
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            System.out.println("Got a track limitation notice:" + numberOfLimitedStatuses);
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
            System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
        }

        @Override
        public void onStallWarning(StallWarning warning) {
            System.out.println("Got stall warning:" + warning);
        }

        @Override
        public void onFriendList(long[] friendIds) {
            System.out.print("onFriendList");
            for (long friendId : friendIds) {
                System.out.print(" " + friendId);
            }
            System.out.println();
        }

        @Override
        public void onFavorite(User source, User target, Status favoritedStatus) {
            System.out.println("onFavorite source:@" + source.getScreenName() + " target:@" + target.getScreenName() + " @" + favoritedStatus.getUser().getScreenName()
                    + " - " + favoritedStatus.getText());
        }

        @Override
        public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
            System.out.println("onUnFavorite source:@" + source.getScreenName() + " target:@" + target.getScreenName() + " @" + unfavoritedStatus.getUser().getScreenName()
                    + " - " + unfavoritedStatus.getText());
        }

        @Override
        public void onFollow(User source, User followedUser) {
            System.out.println("onFollow source:@" + source.getScreenName() + " target:@" + followedUser.getScreenName());
        }

        @Override
        public void onUnfollow(User source, User followedUser) {
            System.out.println("onFollow source:@" + source.getScreenName() + " target:@" + followedUser.getScreenName());
        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {
            System.out.println("onDirectMessage Sender Screen Name:" + directMessage.getSenderScreenName());
            System.out.println("onDirectMessage text:" + directMessage.getText());
            
            String response = "";
            try {
                response = replyDirectMessage(directMessage.getSenderScreenName(), directMessage.getText(),directMessage.getSenderId());
                Twitter twitter = new TwitterFactory().getInstance();
                DirectMessage message = twitter.sendDirectMessage(directMessage.getSenderScreenName(), response);

            } catch (ApiException ae) {
                ae.printStackTrace();
            } catch (TwitterException te) {
                te.printStackTrace();
            }
            // DirectMessage message =
            // sendDirectMessage(directMessage.getSenderScreenName(),
            // directMessage.getText());
        }

        public String replyDirectMessage(String user_name, String user_message, long user_id) throws ApiException {

            String apiKey = "NoB7NVwpKtU.cwA.S8Y.9gSmVTbxanULuDQBYl7p0ILUFlJkAwm65qYo5Tp4Feo";
            ConversationsApi conversationsApi = new ConversationsApi();
            ApiClient client = conversationsApi.getApiClient();
            client.addDefaultHeader("Authorization", "Bearer " + apiKey);

            // Enable Jersey LoggingFilter and you can check contents of
            // requests
            client.setDebugging(true);

            String replyMessage = "";

            System.out.println("@@conversation start");
            Conversation conv = conversationsApi.conversationsStartConversation();
            {
                System.out.println("@@post a conversation message");
                Activity activity = new Activity();
                ChannelAccount channelAccount = new ChannelAccount();
                channelAccount.setName(user_name);
                channelAccount.setId("directline");
                activity.setFrom(channelAccount);
                activity.setType("Message");
                activity.setText(user_message);
                activity.setReplyToId(new Long(user_id).toString());
                ResourceResponse response = conversationsApi.conversationsPostActivity(conv.getConversationId(), activity);

            }

            {
                System.out.println("@@get conversation messages");
                String watermark = "";
                do {
                    ActivitySet activitySet = //
                            conversationsApi.conversationsGetActivities(conv.getConversationId(), watermark);
                    System.out.println("@@activitySet size = " + activitySet.getActivities().size());
                    for (Activity activity : activitySet.getActivities()) {
                        System.out.println("\t" + activity.getFrom().getName() + " says \"" + activity.getText() + "\"");
                        if (!activity.getFrom().getName().equalsIgnoreCase(user_name))
                            return replyMessage = activity.getText();
                    }
                    if (activitySet.getWatermark() == null || watermark.equals(activitySet.getWatermark()) == false)
                        break;
                    watermark = activitySet.getWatermark();
                    System.out.println("\twatermark = " + watermark);
                } while (true);
            }
            System.out.println("@@end");
            return replyMessage;
        }

        @Override
        public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
            System.out.println("onUserListMemberAddition added member:@" + addedMember.getScreenName() + " listOwner:@" + listOwner.getScreenName() + " list:"
                    + list.getName());
        }

        @Override
        public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
            System.out.println("onUserListMemberDeleted deleted member:@" + deletedMember.getScreenName() + " listOwner:@" + listOwner.getScreenName() + " list:"
                    + list.getName());
        }

        @Override
        public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
            System.out.println("onUserListSubscribed subscriber:@" + subscriber.getScreenName() + " listOwner:@" + listOwner.getScreenName() + " list:" + list.getName());
        }

        @Override
        public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
            System.out.println("onUserListUnsubscribed subscriber:@" + subscriber.getScreenName() + " listOwner:@" + listOwner.getScreenName() + " list:" + list.getName());
        }

        @Override
        public void onUserListCreation(User listOwner, UserList list) {
            System.out.println("onUserListCreated  listOwner:@" + listOwner.getScreenName() + " list:" + list.getName());
        }

        @Override
        public void onUserListUpdate(User listOwner, UserList list) {
            System.out.println("onUserListUpdated  listOwner:@" + listOwner.getScreenName() + " list:" + list.getName());
        }

        @Override
        public void onUserListDeletion(User listOwner, UserList list) {
            System.out.println("onUserListDestroyed  listOwner:@" + listOwner.getScreenName() + " list:" + list.getName());
        }

        @Override
        public void onUserProfileUpdate(User updatedUser) {
            System.out.println("onUserProfileUpdated user:@" + updatedUser.getScreenName());
        }

        @Override
        public void onUserDeletion(long deletedUser) {
            System.out.println("onUserDeletion user:@" + deletedUser);
        }

        @Override
        public void onUserSuspension(long suspendedUser) {
            System.out.println("onUserSuspension user:@" + suspendedUser);
        }

        @Override
        public void onBlock(User source, User blockedUser) {
            System.out.println("onBlock source:@" + source.getScreenName() + " target:@" + blockedUser.getScreenName());
        }

        @Override
        public void onUnblock(User source, User unblockedUser) {
            System.out.println("onUnblock source:@" + source.getScreenName() + " target:@" + unblockedUser.getScreenName());
        }

        @Override
        public void onRetweetedRetweet(User source, User target, Status retweetedStatus) {
            System.out.println("onRetweetedRetweet source:@" + source.getScreenName() + " target:@" + target.getScreenName() + retweetedStatus.getUser().getScreenName()
                    + " - " + retweetedStatus.getText());
        }

        @Override
        public void onFavoritedRetweet(User source, User target, Status favoritedRetweet) {
            System.out.println("onFavroitedRetweet source:@" + source.getScreenName() + " target:@" + target.getScreenName() + favoritedRetweet.getUser().getScreenName()
                    + " - " + favoritedRetweet.getText());
        }

        @Override
        public void onQuotedTweet(User source, User target, Status quotingTweet) {
            System.out.println("onQuotedTweet" + source.getScreenName() + " target:@" + target.getScreenName() + quotingTweet.getUser().getScreenName() + " - "
                    + quotingTweet.getText());
        }

        @Override
        public void onException(Exception ex) {
            ex.printStackTrace();
            System.out.println("onException:" + ex.getMessage());
        }
    };

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Hello server</h1>\n";
        Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }
        return newFixedLengthResponse(msg + "</body></html>\n");
    }
}
