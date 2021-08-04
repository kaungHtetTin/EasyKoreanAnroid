package com.calamus.easykorean.app;

public class Routing {
    public static final String DOMAIN_API="https://www.calamuseducation.com/calamus/api";
    //common routes

    public static final String SIGN_UP=DOMAIN_API+"/signup";    //
    public static final String LOGIN=DOMAIN_API+"/login";       //
    public static final String FETCH_LESSONS=DOMAIN_API+"/lessons";//
    public static final String FETCH_VIDEO=DOMAIN_API+"/lessons/video";//
    public static final String FIND_A_VIDEO=DOMAIN_API+"/lessons/video/find";//
    public static final String GET_ANNOUNCEMENT=DOMAIN_API+"/anouncement";//
    public static final String FETCH_DISCUSSION=DOMAIN_API+"/posts/special";//
    public static final String EDIT_PROFILE=DOMAIN_API+"/editprofile";//
    public static final String ADD_COMMENT=DOMAIN_API+"/comments/add";//
    public static final String DELETE_COMMENT=DOMAIN_API+"/comments/delete";//
    public static final String LIKE_A_COMMENT=DOMAIN_API+"/comments/like";//
    public static final String FETCH_COMMENT_LIKE=DOMAIN_API+"/comments/likes";//
    public static final String ADD_POST=DOMAIN_API+"/posts/add";//
    public static final String DELETE_POST=DOMAIN_API+"/posts/delete";//
    public static final String REPORT_POST=DOMAIN_API+"/posts/report";//
    public static final String GET_VIDEO_DATA=DOMAIN_API+"/posts/viewcount";//
    public static final String EDIT_POST=DOMAIN_API+"/posts/edit";//
    public static final String LIKE_A_POST=DOMAIN_API+"/posts/like";//
    public static final String LIKE_A_SONG=DOMAIN_API+"/songs/like";//
    public static final String FETCH_POST_LIKE=DOMAIN_API+"/posts/likes";//
    public static final String GET_SONGS=DOMAIN_API+"/songs/get";//
    public static final String SEARCH_A_SONG=DOMAIN_API+"/songs/search";//
    public static final String DOWNLOAD_A_SONG=DOMAIN_API+"/songs/download";//
    public static final String GET_POPULAR_SONG=DOMAIN_API+"/songs/popular";
    public static final String GET_SONG_LYRICS=DOMAIN_API+"/songs/lyrics/";
    public static final String GET_SONG_ARTIST=DOMAIN_API+"/songs/artists";
    public static final String GET_SONG_BY_ARTIST=DOMAIN_API+"/songs/";
    public static final String RECORD_A_CLICK=DOMAIN_API+"/click/korean";
    public static final String GET_PROFILE=DOMAIN_API+"/getprofile";
    public static final String PLAY_VIDEO=DOMAIN_API+"/playvideo/";
    public static final String SEARCH_MY_ACCOUNT=DOMAIN_API+"/searchaccount/";
    public static final String VERIFY_CODE=DOMAIN_API+"/emailverify/";
    public static final String RESET_PASSWORD_BY_CODE=DOMAIN_API+"/resetpasswordbycode/";
    //app routes
    public static final String GET_FORM=DOMAIN_API+"/form/korea";   //
    public static final String GET_LOGIN_USERDATA=DOMAIN_API+"/login/korean";   //
    public static final String GET_WORD_OF_THE_DAY=DOMAIN_API+"/wordofdays/korean"; //
    public static final String FETCH_POST=DOMAIN_API+"/posts/korean";//
    public static final String FETCH_NOTIFICATION=DOMAIN_API+"/notifications/korean";//
    public static final String FETCH_COMMENT=DOMAIN_API+"/comments/korean";//
    public static final String GET_GAME_WORD=DOMAIN_API+"/game/korean";//
    public static final String GET_GAME_SCORE=DOMAIN_API+"/gamers/korean/scores";//
    public static final String UPDATE_GAME_SCORE=DOMAIN_API+"/gamers/korean/scores/update";//


    public static final String PAYMENT="https://www.calamuseducation.com/easykorean/payment.html";
    public static final String SEND_NOTI="https://www.calamuseducation.com/easykorean/pushnotification/sendnotification.php";
    public static final String APP_UPDATE="https://www.calamuseducation.com/easykorean/versioncontrol.php";
}
