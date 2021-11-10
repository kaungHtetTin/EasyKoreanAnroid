package com.calamus.easykorean.app;

public class Routing {
    public static final String DOMAIN_API="https://www.calamuseducation.com/calamus-v2/api/korea";
    public static final String MAJOR="korea";
    public static final String APP_NAME="Easy Korean";
    public static final String MAJOR_CODE="ko";
    public static final String ADMIN_TOPIC="adminKorea";
    public static final String timerMessage="It's time to learn Korean! Easy Korean is ready for you.";
    //common routes
    public static final String SIGN_UP=DOMAIN_API+"/signup";
    public static final String LOGIN=DOMAIN_API+"/login";
    public static final String GET_LOGIN_USERDATA=DOMAIN_API+"/login/data";
    public static final String EDIT_PROFILE=DOMAIN_API+"/editprofile";
    public static final String GET_PROFILE=DOMAIN_API+"/getprofile";
    public static final String SET_STUDY_TIME=DOMAIN_API+"/setstudytime/"+MAJOR_CODE;

    //lesson routing
    public static final String FETCH_LESSONS=DOMAIN_API+"/lessons";
    public static final String FETCH_VIDEO=DOMAIN_API+"/lessons/video";
    public static final String FIND_A_VIDEO=DOMAIN_API+"/lessons/video/find";


    //announcement routing
    public static final String GET_ANNOUNCEMENT=DOMAIN_API+"/announcement";

    //post routing
    public static final String FETCH_POST=DOMAIN_API+"/posts/"+MAJOR_CODE;
    public static final String FETCH_A_USER_POST=DOMAIN_API+"/posts";
    public static final String ADD_POST=DOMAIN_API+"/posts/add";// post
    public static final String DELETE_POST=DOMAIN_API+"/posts/delete";
    public static final String REPORT_POST=DOMAIN_API+"/posts/report";
    public static final String GET_VIDEO_DATA=DOMAIN_API+"/posts/viewcount";
    public static final String EDIT_POST=DOMAIN_API+"/posts/edit";

    //comment routing
    public static final String FETCH_COMMENT=DOMAIN_API+"/comments/"+MAJOR_CODE;//
    public static final String ADD_COMMENT=DOMAIN_API+"/comments/add";//post
    public static final String DELETE_COMMENT=DOMAIN_API+"/comments/delete";//post

    //like routing
    public static final String LIKE_A_COMMENT=DOMAIN_API+"/comments/like"; //post
    public static final String FETCH_COMMENT_LIKE=DOMAIN_API+"/comments";
    public static final String LIKE_A_POST=DOMAIN_API+"/posts/like";
    public static final String FETCH_POST_LIKE=DOMAIN_API+"/post-like";
    public static final String LIKE_A_SONG=DOMAIN_API+"/songs/like";//


    //song routing
    public static final String GET_SONGS=DOMAIN_API+"/songs/all";
    public static final String SEARCH_A_SONG=DOMAIN_API+"/songs/search";
    public static final String DOWNLOAD_A_SONG=DOMAIN_API+"/songs/download";
    public static final String GET_POPULAR_SONG=DOMAIN_API+"/songs/popular";
    public static final String GET_SONG_BY_ARTIST=DOMAIN_API+"/songs/by";
    public static final String GET_SONG_ARTIST=DOMAIN_API+"/artists";
    public static final String GET_SONG_LYRICS=DOMAIN_API+"/songs/lyrics/";

    //user routing
    public static final String SEARCHING=DOMAIN_API+"/search/";


    //app routing
    public static final String PLAY_VIDEO=DOMAIN_API+"/playvideo/";
    public static final String GET_FORM=DOMAIN_API+"/form";
    public static final String GET_VIDEO_URL=DOMAIN_API+"/posts/videourl/";
    public static final String GET_WORD_OF_THE_DAY=DOMAIN_API+"/wordofdays";
    public static final String PAYMENT=DOMAIN_API+"/payment";

    //password routing
    public static final String SEARCH_MY_ACCOUNT=DOMAIN_API+"/searchaccount/";
    public static final String VERIFY_CODE=DOMAIN_API+"/emailverify/";
    public static final String RESET_PASSWORD_BY_CODE=DOMAIN_API+"/resetpasswordbycode/";
    public static final String RESET_PASSWORD_BY_USER=DOMAIN_API+"/resetpasswordbyuser/";
    public static final String CHECK_CURRENT_PASSWORD=DOMAIN_API+"/checkpassword/";

    //friend routing
    public static final String ADD_FRIEND=DOMAIN_API+"/addfriend";
    public static final String CONFIRM_FRIEND=DOMAIN_API+"/confirmfriend";
    public static final String UN_FRIEND=DOMAIN_API+"/unfriend";
    public static final String REMOVE_FRIEND_REQUEST=DOMAIN_API+"/removefriendrequest";
    public static final String GET_FRIEND_REQUEST=DOMAIN_API+"/getfriendreq/"+MAJOR_CODE;
    public static final String GET_FRIENDS=DOMAIN_API+"/getfriends/"+MAJOR_CODE;

    //notification routing
    public static final String FETCH_NOTIFICATION=DOMAIN_API+"/notifications/"+MAJOR_CODE;
    public static final String PUSH_NOTIFICATION=DOMAIN_API+"/pushnotification";
    public static final String PUSH_NOTIFICATION_TOPIC=DOMAIN_API+"/pushnotification/topic";


    //game routing
    public static final String GET_GAME_SCORE=DOMAIN_API+"/gamers/"+MAJOR_CODE;
    public static final String GET_GAME_WORD=DOMAIN_API+"/gameword/"+MAJOR_CODE;
    public static final String UPDATE_GAME_SCORE=DOMAIN_API+"/gamers/scores/update";



    public static final String GET_APP_ADS=DOMAIN_API+"/appads";
    public static final String CLICK_AD=DOMAIN_API+"/appads/click";
    public static final String APP_UPDATE="https://www.calamuseducation.com/easykorean/versioncontrol.php";


    public static final String STUDY_RECORD_A_LESSON=DOMAIN_API+"/studyalesson";
    public static final String RECORD_A_CLICK=DOMAIN_API+"/click/korean";
}
