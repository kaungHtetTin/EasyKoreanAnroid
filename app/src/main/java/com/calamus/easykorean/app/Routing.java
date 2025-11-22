package com.calamus.easykorean.app;

public class Routing {

    public static final String ADMOB_INTERSTITIAL="ca-app-pub-2472405866346270/8307853906";
    public static final String DOMAIN_API="https://www.calamuseducation.com/calamus-v2/api/korea";
    public static final String MAJOR="korea";
    public static final String APP_NAME="Easy Korean";
    public static final String MAJOR_CODE="ko";
    public static final String PUSH_NOTIFICATION_TO_TEACHER="ekTeacher";
    public static final String PUSH_NOTIFICATION_TO_DEVELOPER="ekDeveloper";
    public static final String timerMessage="It's time to learn Korean! Easy Korean is ready for you.";
    public static final String subscribeToTopic="koreaUsers";
    public static final String FLASHCARD_LANGUAGE_ID = "1";

    //course routes
    public static final String GET_COURSE_RATING=DOMAIN_API+"/course/reviews";


    //rating routes
    public static final String RATING=DOMAIN_API+"/rates";
    public static final String DELETE_RATING=DOMAIN_API+"/rates/delete";


    //common routes
    public static final String CHECK_ACCOUNT=DOMAIN_API+"/checkaccount";
    public static final String SIGN_UP=DOMAIN_API+"/signup";
    public static final String LOGIN=DOMAIN_API+"/login";
    public static final String GET_LOGIN_USERDATA=DOMAIN_API+"/login/data";
    public static final String EDIT_PROFILE=DOMAIN_API+"/editprofile";
    public static final String GET_PROFILE=DOMAIN_API+"/getprofile";
    public static final String SET_STUDY_TIME=DOMAIN_API+"/setstudytime";
    public static final String CHANGE_COVER_PHOTO=DOMAIN_API+"/coverphoto";
    public static final String CHANGE_BIO=DOMAIN_API+"/updatebio";
    public static final String DELETE_ACCOUNT=DOMAIN_API+"/delete-account";
    public static final String REQUEST_TOKEN=DOMAIN_API+"/request-auth-token";

    //lesson routing
    public static final String FETCH_LESSONS=DOMAIN_API+"/lessons";
    public static final String FETCH_VIDEO=DOMAIN_API+"/lessons/video";
    public static final String FIND_A_VIDEO=DOMAIN_API+"/lessons/search/video";
    public static final String GET_DAY_PLAN=DOMAIN_API+"/lessons/dayplan?";
    public static final String GET_CATEGORY_BY_COURSE=DOMAIN_API+"/lessons/category";
    public static final String GET_LESSONS_BY_DAY_PLAN=DOMAIN_API+"/lessons-by-day?";


    //announcement routing
    public static final String GET_ANNOUNCEMENT=DOMAIN_API+"/announcement";

    //post routing
    public static final String FETCH_POST=DOMAIN_API+"/posts";
    public static final String FETCH_A_USER_POST=DOMAIN_API+"/posts/user-share";
    public static final String ADD_POST=DOMAIN_API+"/posts/add";// post
    public static final String DELETE_POST=DOMAIN_API+"/posts/delete";
    public static final String REPORT_POST=DOMAIN_API+"/posts/report";
    public static final String GET_VIDEO_DATA=DOMAIN_API+"/posts/viewcount";
    public static final String EDIT_POST=DOMAIN_API+"/posts/edit";
    public static final String GET_VIDEO_URL=DOMAIN_API+"/posts/videourl";
    public static final String GET_SHARE_CONTENT=DOMAIN_API+"/posts/share-content";
    public static final String HIDE_POST=DOMAIN_API+"/posts/hide";

    //comment routing
    public static final String FETCH_COMMENT=DOMAIN_API+"/comments";
    public static final String ADD_COMMENT=DOMAIN_API+"/comments/add";//post
    public static final String DELETE_COMMENT=DOMAIN_API+"/comments/delete";//post

    //like routing
    public static final String LIKE_A_COMMENT=DOMAIN_API+"/comments/like"; //post
    public static final String FETCH_COMMENT_LIKE=DOMAIN_API+"/comments/likes";
    public static final String LIKE_A_POST=DOMAIN_API+"/posts/like";
    public static final String FETCH_POST_LIKE=DOMAIN_API+"/posts/likes";
    public static final String LIKE_A_SONG=DOMAIN_API+"/songs/like";//


    //song routing
    public static final String GET_SONGS=DOMAIN_API+"/songs";
    public static final String SEARCH_A_SONG=DOMAIN_API+"/songs/search";
    public static final String DOWNLOAD_A_SONG=DOMAIN_API+"/songs/download";
    public static final String GET_POPULAR_SONG=DOMAIN_API+"/songs/popular";
    public static final String GET_SONG_BY_ARTIST=DOMAIN_API+"/songs/by/artist";
    public static final String GET_POPULAR_SONG_BY_ARTIST=DOMAIN_API+"/songs/by/artist/popular";
    public static final String GET_SONG_ARTIST=DOMAIN_API+"/songs/artists";
    public static final String GET_SONG_LYRICS=DOMAIN_API+"/songs/lyrics/";

    //user routing
    public static final String SEARCHING=DOMAIN_API+"/search?mCode="+MAJOR_CODE+"&search=";
    public static final String BLOCK_USER=DOMAIN_API+"/learners/block";
    public static final String UNBLOCK_USER=DOMAIN_API+"/learners/unblock";


    //app routing
    public static final String PLAY_VIDEO=DOMAIN_API+"/vimeo";
    public static final String GET_WORD_OF_THE_DAY=DOMAIN_API+"/wordofdays";
    public static final String PAYMENT=DOMAIN_API+"/payment";
    public static final String GET_APP_FORM=DOMAIN_API+"/appform";

    //password routing
    public static final String SEARCH_MY_ACCOUNT=DOMAIN_API+"/searchaccount";
    public static final String VERIFY_CODE=DOMAIN_API+"/emailverify";
    public static final String RESET_PASSWORD_BY_CODE=DOMAIN_API+"/resetpasswordbycode";
    public static final String RESET_PASSWORD_BY_USER=DOMAIN_API+"/resetpasswordbyuser";
    public static final String CHECK_CURRENT_PASSWORD=DOMAIN_API+"/checkpassword";

    //friend routing
    public static final String ADD_FRIEND=DOMAIN_API+"/addfriend";
    public static final String CONFIRM_FRIEND=DOMAIN_API+"/confirmfriend";
    public static final String UN_FRIEND=DOMAIN_API+"/unfriend";
    public static final String REMOVE_FRIEND_REQUEST=DOMAIN_API+"/removefriendrequest";
    public static final String GET_FRIEND_REQUEST=DOMAIN_API+"/getfriendrequests?mCode="+MAJOR_CODE+"&userId=";
    public static final String GET_FRIENDS=DOMAIN_API+"/getfriends?mCode="+MAJOR_CODE+"&userId=";

    //notification routing
    public static final String FETCH_NOTIFICATION=DOMAIN_API+"/notifications?mCode="+MAJOR_CODE+"&userId=";
    public static final String PUSH_NOTIFICATION=DOMAIN_API+"/pushnotification";
    public static final String PUSH_NOTIFICATION_TOPIC=DOMAIN_API+"/pushnotification/topic";

    //game routing
    public static final String GET_GAME_SCORE=DOMAIN_API+"/gamers?mCode="+MAJOR_CODE;
    public static final String GET_GAME_WORD=DOMAIN_API+"/gameword?mCode="+MAJOR_CODE;
    public static final String UPDATE_GAME_SCORE=DOMAIN_API+"/gamers/scores/update";



    public static final String START_A_COURSE=DOMAIN_API+"/start/course";
    public static final String GET_COURSE_ENROLL=DOMAIN_API+"/course/enroll";

    public static final String GET_APP_ADS=DOMAIN_API+"/appads/";
    public static final String CLICK_AD=DOMAIN_API+"/appads/click";
    public static final String APP_UPDATE="https://www.calamuseducation.com/easykorean/versioncontrol.php";

    public static final String STUDY_RECORD_A_LESSON=DOMAIN_API+"/studyalesson";
    public static final String RECORD_A_CLICK=DOMAIN_API+"/click/korean";
    public static final String TERMS_OF_USE="https://www.calamuseducation.com/calamus-v2/terms-of-use";
    public static final String PRIVACY_POLICY="https://www.calamuseducation.com/calamus-v2/privacy-policy";

    public static final String FC_GET_PROGRESS = "https://www.calamuseducation.com/calamus/api/vocab-learning/get-vocab-progress.php";
    public static final String FC_GET_CARDS = "https://www.calamuseducation.com/calamus/api/vocab-learning/get-cards.php";
    public static final String FC_RATE_WORD = "https://www.calamuseducation.com/calamus/api/vocab-learning/rate-word.php";
    public static final String FC_SKIP_WORD = "https://www.calamuseducation.com/calamus/api/vocab-learning/skip-word.php";
}
