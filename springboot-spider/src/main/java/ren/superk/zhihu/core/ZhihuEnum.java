package ren.superk.zhihu.core;

import ren.superk.zhihu.model.*;

public enum  ZhihuEnum {
    FOLLOWEES("他关注的人","followees","data[*].locations,employments,gender,educations,business,voteup_count,thanked_Count,follower_count,following_count,cover_url,following_topic_count,following_question_count,following_favlists_count,following_columns_count,avatar_hue,answer_count,articles_count,pins_count,question_count,columns_count,commercial_question_count,favorite_count,favorited_count,logs_count,marked_answers_count,marked_answers_text,message_thread_token,account_status,is_active,is_bind_phone,is_force_renamed,is_bind_sina,is_privacy_protected,sina_weibo_url,sina_weibo_name,show_sina_weibo,is_blocking,is_blocked,is_following,is_followed,mutual_followees_count,vote_to_count,vote_from_count,thank_to_count,thank_from_count,thanked_count,description,hosted_live_count,participated_live_count,allow_message,industry_category,org_name,org_homepage,badge[?(type=best_answerer)].topics","zhihu","people",false, People.class, ZhihuPeoplePager.class,"url_token"),
//    ACTIVITIES("动态","activities"),
    ANSWERS("回答","answers","data[*].is_normal,admin_closed_comment,reward_info,is_collapsed,annotation_action,annotation_detail,collapse_reason,collapsed_by,suggest_edit,comment_count,can_comment,content,voteup_count,reshipment_settings,comment_permission,mark_infos,created_time,updated_time,review_info,question,excerpt,relationship.is_authorized,voting,is_author,is_thanked,is_nothelp,upvoted_followees;data[*].author.badge[?(type=best_answerer)].topics","answer","v1",true, Answer.class, ZhihuAnswerPager.class,"id"),
    QUESTIONS("提问","questions","data[*].created,answer_count,follower_count,author,admin_closed_comment","question","v1",false, Question.class, ZhihuQuestionPager.class,"id"),
    ARTICLES("文章","articles","data[*].comment_count,can_comment,comment_permission,admin_closed_comment,content,voteup_count,created,updated,upvoted_followees,voting,review_info;data[*].author.badge[?(type=best_answerer)].topics","article","v1",true, Article.class, ZhihuArticlePager.class,"id"),
    COLUMN_CONTRIBUTIONS("专栏","column-contributions","data[*].column.intro,followers,articles_count,title,intro,description,image_url,articles_count,followers,is_following,last_article.created","columns","v1",false, Columns.class, ZhihuColumnsPager.class,"id"),
    PINS("想法","pins","data[*].upvoted_followees,admin_closed_comment","pins","v1",false, Pins.class, ZhihuPinsPager.class,"id"),
    FAVLISTS("收藏","favlists","data[*].updated_time,answer_count,follower_count,is_public","fav","v1",false, Fav.class, ZhihuFavPager.class,"id"),
    FOLLOWING_TOPIC_CONTRIBUTIONS("他关注的话题","following-topic-contributions","data[*].topic.introduction,questions_count,best_answers_count,followers_count,is_following","topic","v1",false, Topic.class, ZhihuTopicPager.class,"id"),
    FOLLOWING_COLUMNS("他关注的专栏","following-columns","data[*].title,intro,description,image_url,articles_count,followers,is_following,last_article.created","columns","v1",false, Columns.class, ZhihuColumnsPager.class,"id"),
    FOLLOWING_QUESTIONS("他关注的问题","following-questions","data[*].created,answer_count,follower_count,author,admin_closed_comment","question","v1",false, Question.class, ZhihuQuestionPager.class,"id"),
    FOLLOWING_FAVLISTS("他关注的话题","following-favlists","data[*].updated_time,answer_count,follower_count,is_public","fav","v1",false, Fav.class, ZhihuFavPager.class,"id"),
    FOLLOWERS("关注他的人","followers","data[*].locations,employments,gender,educations,business,voteup_count,thanked_Count,follower_count,following_count,cover_url,following_topic_count,following_question_count,following_favlists_count,following_columns_count,avatar_hue,answer_count,articles_count,pins_count,question_count,columns_count,commercial_question_count,favorite_count,favorited_count,logs_count,marked_answers_count,marked_answers_text,message_thread_token,account_status,is_active,is_bind_phone,is_force_renamed,is_bind_sina,is_privacy_protected,sina_weibo_url,sina_weibo_name,show_sina_weibo,is_blocking,is_blocked,is_following,is_followed,mutual_followees_count,vote_to_count,vote_from_count,thank_to_count,thank_from_count,thanked_count,description,hosted_live_count,participated_live_count,allow_message,industry_category,org_name,org_homepage,badge[?(type=best_answerer)].topics","zhihu","people",false, People.class, ZhihuPeoplePager.class,"url_token"),
//    LOGS("他参与的共工编辑","logs"),
//    ANSWERS_MARKED("他被知乎收录的回答","answers-marked"),
//    LIVES("lives","lives"),
            ;
    //查询路径

    private String name;
    private String value;
    //查询用参数
    private String include;
    private String index;
    private String type;
    private Boolean sortFlag;
    private Class modelClazz;
    private Class modelPageClazz;
    private String pk;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    ZhihuEnum(String name, String value, String include, String index, String type, Boolean sortFlag, Class modelClazz, Class modelPageClazz, String pk) {
        this.name = name;
        this.value = value;
        this.include = include;
        this.index = index;
        this.type = type;
        this.sortFlag = sortFlag;
        this.modelClazz = modelClazz;
        this.modelPageClazz = modelPageClazz;
        this.pk = pk;
    }

    public static ZhihuEnum byVal(String val){
        if(val == null) return null;
        for (ZhihuEnum zhihuEnum : ZhihuEnum.values()) {
            if(val.equals(zhihuEnum.value)){
                return zhihuEnum;
            }
        }
        return null;
    }


    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getSortFlag() {
        return sortFlag;
    }

    public void setSortFlag(Boolean sortFlag) {
        this.sortFlag = sortFlag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ZhihuEnum(String name, String value) {
        this.value = value;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Class getModelClazz() {
        return modelClazz;
    }

    public void setModelClazz(Class modelClazz) {
        this.modelClazz = modelClazz;
    }

    public Class getModelPageClazz() {
        return modelPageClazz;
    }

    public void setModelPageClazz(Class modelPageClazz) {
        this.modelPageClazz = modelPageClazz;
    }
}
