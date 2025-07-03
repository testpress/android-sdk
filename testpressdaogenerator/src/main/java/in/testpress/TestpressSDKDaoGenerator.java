package in.testpress;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Index;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToMany;
import org.greenrobot.greendao.generator.ToOne;

public class TestpressSDKDaoGenerator {
    // Increase the version if any modification has been made in this file.
    private static final int VERSION = 74;

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(VERSION, "in.testpress.models.greendao");
        Entity reviewAttempt = addReviewAttempt(schema);
        Entity reviewItem = addReviewItem(schema);
        Entity reviewQuestion = addReviewQuestion(schema);
        Entity reviewAnswer = addReviewAnswer(schema);
        Entity reviewQuestionTranslation = addReviewQuestionTranslation(schema);
        Entity reviewAnswerTranslation = addReviewAnswerTranslation(schema);
        Entity userUploadedFile = addUserUploadedFile(schema);
        addSelectedAnswer(schema);
        addAttemptToReviewItem(reviewAttempt, reviewItem);
        addReviewItemToQuestion(reviewItem, reviewQuestion);
        addReviewQuestionToAnswers(reviewQuestion, reviewAnswer);
        addTranslationsToReviewQuestion(reviewQuestion, reviewQuestionTranslation);
        addAnswersToReviewTranslations(reviewQuestionTranslation, reviewAnswerTranslation);
        addReviewItemToUserUploadedFiles(reviewItem, userUploadedFile);

        Entity direction = addDirection(schema);
        Entity examQuestion = addExamQuestion(schema);
        Entity question = addQuestion(schema);
        Entity answer = addAnswer(schema);
        addQuestionToExamQuestion(examQuestion, question);
        addQuestionToAnswers(question, answer);
        addDirectionToQuestion(direction, question);

        Entity userSelectedAnswer = addUserSelectedAnswer(schema);
        addQuestionToUserSelectedAnswer(question, userSelectedAnswer);
        addExamQuestionToUserSelectedAnswer(examQuestion, userSelectedAnswer);

        Entity course = addCourse(schema);

        Entity chapter = addChapter(schema);
        Property order = chapter.addIntProperty("order").getProperty();
        addChaptersToCourse(course, chapter, order);
        addChildrenToChapter(chapter, chapter, order);

        Entity content = addContent(schema);
        addContentsToCourse(course, content);
        addChapterRelationToContent(chapter, content);

        Entity html = addHtmlContent(schema);
        Entity video = addVideo(schema);
        Entity stream = addStream(schema);
        Entity attachment = addAttachment(schema);
        Entity exam = addExam(schema);
        Entity videoConference = addVideoConference(schema);
        Entity liveStream = addLiveStream(schema);
        addLanguage(schema, exam);
        addVideoConferenceToContent(content, videoConference);
        addLiveStreamToContent(content, liveStream);
        addHTMLToContent(content, html);
        addVideoToContent(content, video);
        addStreamToVideo(stream, video);
        addAttachmentToContent(content, attachment);
        addExamToContent(content, exam);
        addStreamIdToVideoContent(video, stream);


        Entity courseAttempt = addCourseAttempt(schema);
        Entity attempt = addAttempt(schema);
        addCourseContentToCourseAttempt(courseAttempt, content);
        addAttemptToCourseAttempt(courseAttempt, attempt);
        Entity videoAttempt = addVideoAttempt(schema);
        addVideoAttemptToCourseAttempt(courseAttempt, videoAttempt);
        addVideoToVideoAttempt(videoAttempt, video);
        Entity attemptSection = addAttemptSection(schema);
        addSectionToAttempt(attempt, attemptSection);

        addBookmarkFolder(schema);
        Entity bookmark = addBookmark(schema);
        Entity contentType = addContentType(schema);
        addContentTypeToBookmark(bookmark, contentType);

        Entity answerTranslation = addAnswerTranslation(schema);
        addAnswersTranslationToReviewTranslations(reviewQuestionTranslation, answerTranslation);
        addSubject(schema);
        addDirectionTranslation(schema);

        Entity product = addProduct(schema);
        Entity price = addPrice(schema);
        addProductToPrice(price, product);

        schema.enableKeepSectionsByDefault();
        new DaoGenerator().generateAll(schema, "core/src/main/java");
    }

    private static Entity addUserUploadedFile(Schema schema) {
        Entity userUploadedFile = schema.addEntity("UserUploadedFile");
        userUploadedFile.addLongProperty("id").primaryKey();
        userUploadedFile.addStringProperty("path");
        userUploadedFile.addStringProperty("pdfPreviewUrl");
        userUploadedFile.addStringProperty("url");
        return userUploadedFile;
    }

    private static void addProductToPrice(Entity price, Entity product) {
        Property productId = price.addLongProperty("productId").getProperty();
        price.addToOne(product, productId, "product");
    }

    private static Entity addPrice(Schema schema) {
        Entity price = schema.addEntity("Price");
        price.addLongProperty("id").primaryKey();
        price.addStringProperty("name");
        price.addStringProperty("price");
        price.addIntProperty("validity");
        price.addStringProperty("start_date");
        price.addStringProperty("end_date");
        return price;
    }

    private static Entity addProduct(Schema schema) {
        Entity product = schema.addEntity("Product");
        product.addLongProperty("id").primaryKey();
        product.addStringProperty("title");
        product.addStringProperty("slug");
        product.addStringProperty("descriptionHtml");
        product.addStringProperty("image");
        product.addStringProperty("startDate");
        product.addStringProperty("endDate");
        product.addStringProperty("buyNowText");
        product.addStringProperty("surl");
        product.addStringProperty("furl");
        product.addStringProperty("currentPrice");
        product.addStringProperty("prices").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        product.addStringProperty("courseIds").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        ).codeBeforeField("@SerializedName(\"courses\")");
        product.addLongProperty("order");
        product.addStringProperty("category");
        return product;
    }

    private static Entity addUserSelectedAnswer(Schema schema) {
        Entity userSelectedAnswer = schema.addEntity("UserSelectedAnswer");
        userSelectedAnswer.addLongProperty("id").primaryKey();
        userSelectedAnswer.addIntProperty("order");
        userSelectedAnswer.addBooleanProperty("review");
        userSelectedAnswer.addLongProperty("examId");
        userSelectedAnswer.addLongProperty("attemptId");
        userSelectedAnswer.addStringProperty("explanationHtml");
        userSelectedAnswer.addStringProperty("shortText");
        userSelectedAnswer.addStringProperty("duration");
        userSelectedAnswer.addStringProperty("selectedAnswers").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        userSelectedAnswer.addStringProperty("correctAnswers").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        userSelectedAnswer.addStringProperty("url");
        return userSelectedAnswer;
    }

    private static void addQuestionToUserSelectedAnswer(Entity question, Entity userSelectedAnswer) {
        Property questionId = userSelectedAnswer.addLongProperty("questionId").getProperty();
        userSelectedAnswer.addToOne(question, questionId, "question");
    }

    private static void addExamQuestionToUserSelectedAnswer(Entity examQuestion, Entity userSelectedAnswer) {
        Property examQuestionId = userSelectedAnswer.addLongProperty("examQuestionId").getProperty();
        userSelectedAnswer.addToOne(examQuestion, examQuestionId, "examQuestion");
    }

    private static Entity addExamQuestion(Schema schema) {
        Entity examQuestion = schema.addEntity("ExamQuestion");
        examQuestion.addLongProperty("id").primaryKey();
        examQuestion.addIntProperty("order");
        examQuestion.addLongProperty("examId");
        examQuestion.addLongProperty("attemptId");
        return examQuestion;
    }

    private static void addQuestionToExamQuestion(Entity examQuestion, Entity question) {
        Property questionId = examQuestion.addLongProperty("questionId").getProperty();
        examQuestion.addToOne(question, questionId, "question");
    }

    private static Entity addQuestion(Schema schema) {
        Entity question = schema.addEntity("Question");
        question.addLongProperty("id").primaryKey();
        question.addStringProperty("questionHtml");
        question.addStringProperty("directionHtml");
        question.addLongProperty("parentId");
        question.addStringProperty("type");
        question.addStringProperty("language");
        question.addStringProperty("explanationHtml");
        question.addStringProperty("commentsUrl");
        question.addStringProperty("percentageGotCorrect");
        question.addStringProperty("answerIds").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        return question;
    }

    private static void addDirectionToQuestion(Entity direction, Entity question) {
        Property directionId = question.addLongProperty("directionId").getProperty();
        question.addToOne(direction, directionId, "direction");
    }

    private static Entity addAnswer(Schema schema) {
        Entity answer = schema.addEntity("Answer");
        answer.addLongProperty("id").primaryKey();
        answer.addStringProperty("textHtml");
        answer.addBooleanProperty("isCorrect");
        answer.addStringProperty("marks");
        return answer;
    }

    private static void addQuestionToAnswers(Entity question, Entity answer) {
        Property questionId = answer.addLongProperty("questionId").getProperty();
        ToMany questionToAnswers = question.addToMany(answer, questionId);
        questionToAnswers.setName("answers");
    }

    private static Entity addAttempt(Schema schema) {
        Entity attempt = schema.addEntity("Attempt");
        attempt.addStringProperty("url");
        attempt.addLongProperty("id").primaryKey();
        attempt.addStringProperty("date");
        attempt.addIntProperty("totalQuestions");
        attempt.addStringProperty("score");
        attempt.addStringProperty("rank");
        attempt.addStringProperty("maxRank");
        attempt.addStringProperty("reviewUrl");
        attempt.addStringProperty("questionsUrl");
        attempt.addIntProperty("correctCount");
        attempt.addIntProperty("incorrectCount");
        attempt.addStringProperty("lastStartedTime");
        attempt.addStringProperty("remainingTime");
        attempt.addStringProperty("timeTaken");
        attempt.addStringProperty("state");
        attempt.addStringProperty("percentile");
        attempt.addIntProperty("speed");
        attempt.addIntProperty("accuracy");
        attempt.addStringProperty("percentage");
        attempt.addIntProperty("lastViewedQuestionId");
        attempt.addStringProperty("externalReviewUrl");
        attempt.addStringProperty("reviewPdf");
        attempt.addBooleanProperty("rankEnabled");
        attempt.addIntProperty("attemptType");
        attempt.implementsInterface("android.os.Parcelable");
        return attempt;

    }

    private static Entity addCourseAttempt(Schema schema) {
        Entity courseAttempt = schema.addEntity("CourseAttempt");
        courseAttempt.addLongProperty("id").primaryKey();
        courseAttempt.addStringProperty("type");
        courseAttempt.addIntProperty("objectId");
        courseAttempt.addStringProperty("objectUrl");
        courseAttempt.addStringProperty("trophies");
        courseAttempt.implementsInterface("android.os.Parcelable");
        return courseAttempt;
    }

    private static void addCourseContentToCourseAttempt(Entity courseAttempt, Entity content) {
        Property courseContentId = courseAttempt.addLongProperty("chapterContentId").getProperty();
        courseAttempt.addToOne(content, courseContentId, "chapterContent");
    }

    private static void addAttemptToCourseAttempt(Entity courseAttempt, Entity attempt) {
        Property attemptId = courseAttempt.addLongProperty("assessmentId").getProperty();
        courseAttempt.addToOne(attempt, attemptId, "assessment");
    }

    private static void addVideoAttemptToCourseAttempt(Entity courseAttempt, Entity videoAttempt) {
        Property videoAttemptId = courseAttempt.addLongProperty("userVideoId").getProperty();
        courseAttempt.addToOne(videoAttempt, videoAttemptId, "video");
    }

    private static void addVideoToVideoAttempt(Entity videoAttempt, Entity videoContent) {
        Property videoContentId = videoAttempt.addLongProperty("videoContentId").getProperty();
        videoAttempt.addToOne(videoContent, videoContentId, "videoContent");
    }

    private static void addExamToContent(Entity content, Entity exam) {
        Property examId = content.addLongProperty("examId").getProperty();
        content.addToOne(exam, examId, "exam");
    }

    private static void addVideoConferenceToContent(Entity content, Entity videoConference) {
        Property htmlId = content.addLongProperty("videoConferenceId").getProperty();
        content.addToOne(videoConference, htmlId, "videoConference");
    }

    private static void addLiveStreamToContent(Entity content, Entity liveStream) {
        Property liveStreamId = content.addLongProperty("liveStreamId").getProperty();
        content.addToOne(liveStream, liveStreamId, "liveStream");
    }


    private static void addHTMLToContent(Entity content, Entity html) {
        Property htmlId = content.addLongProperty("htmlId").getProperty();
        content.addToOne(html, htmlId, "htmlContent");
    }

    private static void addVideoToContent(Entity content, Entity video) {
        Property videoId = content.addLongProperty("videoId").getProperty();
        content.addToOne(video, videoId, "video");
    }

    private static void addAttachmentToContent(Entity content, Entity attachment) {
        Property attachmentId = content.addLongProperty("attachmentId").getProperty();
        content.addToOne(attachment, attachmentId, "attachment");
    }

    private static Entity addHtmlContent(Schema schema) {
        Entity htmlContent = schema.addEntity("HtmlContent");
        htmlContent.addLongProperty("id").primaryKey();
        htmlContent.addStringProperty("title");
        htmlContent.addStringProperty("textHtml");
        htmlContent.addStringProperty("sourceUrl");
        htmlContent.addStringProperty("readTime");
        return htmlContent;
    }

    private static void addStreamIdToVideoContent(Entity video, Entity stream) {
        Property streamId = video.addLongProperty("streamId").getProperty();
        video.addToOne(stream, streamId, "stream");
    }

    private static Entity addContent(Schema schema) {
        Entity content = schema.addEntity("Content");
        content.addIntProperty("order");
        content.addStringProperty("htmlContentTitle");
        content.addStringProperty("htmlContentUrl")
                .codeBeforeField("@SerializedName(value=\"html_content_url\", alternate={\"html_url\"})");;
        content.addStringProperty("url");
        content.addStringProperty("attemptsUrl");
        content.addStringProperty("chapterSlug");
        content.addStringProperty("chapterUrl");
        content.addLongProperty("id").primaryKey();
        content.addStringProperty("title")
                .codeBeforeField("@SerializedName(value=\"title\", alternate={\"name\"})");

        content.addStringProperty("contentType");
        content.addStringProperty("image");
        content.addStringProperty("description");
        content.addBooleanProperty("isLocked");
        content.addIntProperty("attemptsCount").notNull();
        content.addStringProperty("start");
        content.addStringProperty("end");
        content.addBooleanProperty("hasStarted");
        content.addBooleanProperty("active");
        content.addLongProperty("bookmarkId");
        content.addIntProperty("videoWatchedPercentage").notNull();
        content.addStringProperty("modified");
        content.addLongProperty("modifiedDate");
        content.addBooleanProperty("freePreview");
        content.addBooleanProperty("isScheduled");
        content.addStringProperty("coverImage");
        content.addStringProperty("coverImageMedium");
        content.addStringProperty("coverImageSmall");
        content.addBooleanProperty("isCourseAvailable");
        content.addLongProperty("nextContentId");
        content.addBooleanProperty("hasEnded");
        content.addStringProperty("examStartUrl");
        content.implementsInterface("android.os.Parcelable");
        return content;
    }

    private static void addChaptersToCourse(Entity course, Entity chapter, Property order) {
        Property courseId = chapter.addLongProperty("courseId").getProperty();
        ToMany courseToChapters = course.addToMany(chapter, courseId);
        courseToChapters.setName("chapters");
        courseToChapters.orderAsc(order);
    }

    private static void addChildrenToChapter(Entity parentChapter, Entity chapter, Property order) {
        Property parentId = chapter.addLongProperty("parentId").getProperty();
        ToMany childrenToChapters = parentChapter.addToMany(chapter, parentId);
        childrenToChapters.setName("children");
        childrenToChapters.orderAsc(order);
    }

    private static void addChapterRelationToContent(Entity chapter, Entity content) {
        Property chapterId = content.addLongProperty("chapterId").getProperty();
        ToMany chapterToContents = chapter.addToMany(content, chapterId);
        chapterToContents.setName("contents");

        content.addToOne(chapter, chapterId, "chapter");
    }


    private static void addContentsToCourse(Entity course, Entity content) {
        Property courseId = content.addLongProperty("courseId").getProperty();
        ToMany courseToContents = course.addToMany(content, courseId);
        courseToContents.setName("contents");
    }

    private static Entity addAttachment(Schema schema) {
        Entity attachment = schema.addEntity("Attachment");
        attachment.addStringProperty("title");
        attachment.addStringProperty("attachmentUrl");
        attachment.addStringProperty("description");
        attachment.addLongProperty("id").primaryKey();
        attachment.addBooleanProperty("isRenderable");
        attachment.implementsInterface("android.os.Parcelable");
        return attachment;
    }

    private static Entity addVideoConference(Schema schema) {
        Entity video = schema.addEntity("VideoConference");
        video.addStringProperty("title");
        video.addStringProperty("joinUrl");
        video.addLongProperty("id").primaryKey();
        video.addStringProperty("start");
        video.addIntProperty("duration");
        video.addStringProperty("provider");
        video.addStringProperty("conferenceId");
        video.addStringProperty("accessToken");
        video.addStringProperty("password");
        video.addBooleanProperty("showRecordedVideo");
        video.addStringProperty("state");
        return video;
    }

    private static Entity addLiveStream(Schema schema) {
        Entity liveStream = schema.addEntity("LiveStream");
        liveStream.addLongProperty("id").primaryKey();
        liveStream.addStringProperty("title");
        liveStream.addStringProperty("streamUrl");
        liveStream.addIntProperty("duration");
        liveStream.addStringProperty("status");
        liveStream.addBooleanProperty("showRecordedVideo");
        liveStream.addStringProperty("chatEmbedUrl");
        return liveStream;
    }

    private static Entity addVideo(Schema schema) {
        Entity video = schema.addEntity("Video");
        video.addStringProperty("title");
        video.addStringProperty("url");
        video.addLongProperty("id").primaryKey();
        video.addStringProperty("embedCode");
        video.addStringProperty("duration");
        video.addBooleanProperty("isDomainRestricted");
        video.addStringProperty("thumbnail");
        video.addStringProperty("thumbnailMedium");
        video.addStringProperty("thumbnailSmall");
        video.addBooleanProperty("isViewsExhausted");
        video.addStringProperty("transcodingStatus");
        video.implementsInterface("android.os.Parcelable");
        return video;
    }

    private static Entity addVideoAttempt(Schema schema) {
        Entity videoAttempt = schema.addEntity("VideoAttempt");
        videoAttempt.addLongProperty("id").primaryKey();
        videoAttempt.addStringProperty("lastPosition");
        videoAttempt.addIntProperty("state");
        videoAttempt.addStringProperty("watchedDuration");
        return videoAttempt;
    }

    private static Entity addLanguage(Schema schema, Entity exam) {
        Entity language = schema.addEntity("Language");
        language.addLongProperty("id").primaryKey().autoincrement();
        Property code = language.addStringProperty("code").getProperty();
        language.addStringProperty("title");
        language.implementsInterface("android.os.Parcelable");

        // Add languages to exam
        Property examId = language.addLongProperty("examId").getProperty();
        exam.addToMany(language, examId, "languages");

        // Unique together code & examId
        Index index = new Index();
        index.addProperty(code);
        index.addProperty(examId);
        index.makeUnique();

        language.addIndex(index);
        return language;
    }

    private static Entity addExam(Schema schema) {
        Entity exam = schema.addEntity("Exam");
        exam.addStringProperty("totalMarks");
        exam.addStringProperty("url");
        exam.addLongProperty("id").primaryKey();
        exam.addIntProperty("attemptsCount");
        exam.addIntProperty("pausedAttemptsCount");
        exam.addStringProperty("title");
        exam.addStringProperty("description");
        exam.addStringProperty("startDate");
        exam.addStringProperty("endDate");
        exam.addStringProperty("duration");
        exam.addIntProperty("numberOfQuestions");
        exam.addStringProperty("negativeMarks");
        exam.addStringProperty("markPerQuestion");
        exam.addIntProperty("templateType");
        exam.addBooleanProperty("allowRetake");
        exam.addBooleanProperty("allowPdf");
        exam.addBooleanProperty("showAnswers");
        exam.addIntProperty("maxRetakes");
        exam.addStringProperty("attemptsUrl");
        exam.addStringProperty("deviceAccessControl");
        exam.addIntProperty("commentsCount");
        exam.addStringProperty("slug");
        exam.addStringProperty("selectedLanguage");
        exam.addBooleanProperty("variableMarkPerQuestion");
        exam.addIntProperty("passPercentage");
        exam.addBooleanProperty("enableRanks");
        exam.addBooleanProperty("showScore");
        exam.addBooleanProperty("showPercentile");
        exam.addStringProperty("categories").customType(
                "in.testpress.util.StringList",
                "in.testpress.util.StringListConverter"
        );
        exam.addBooleanProperty("isDetailsFetched");
        exam.addBooleanProperty("isGrowthHackEnabled");
        exam.addStringProperty("shareTextForSolutionUnlock");
        exam.addBooleanProperty("showAnalytics");
        exam.addStringProperty("instructions");
        exam.addBooleanProperty("hasAudioQuestions");
        exam.addStringProperty("rankPublishingDate");
        exam.addBooleanProperty("enableQuizMode");
        exam.addBooleanProperty("disableAttemptResume");
        exam.addBooleanProperty("allowPreemptiveSectionEnding");
        exam.addStringProperty("examDataModifiedOn");
        exam.addBooleanProperty("isOfflineExam");
        exam.addLongProperty("graceDurationForOfflineSubmission");
        exam.addBooleanProperty("enableExamWindowMonitoring");
        exam.implementsInterface("android.os.Parcelable");
        return exam;
    }

    private static Entity addAttemptSection(Schema schema) {
        Entity attemptSection = schema.addEntity("AttemptSection");
        attemptSection.addLongProperty("id");
        attemptSection.addLongProperty("attemptSectionId").primaryKey();
        attemptSection.addStringProperty("state");
        attemptSection.addStringProperty("questionsUrl");
        attemptSection.addStringProperty("startUrl");
        attemptSection.addStringProperty("endUrl");
        attemptSection.addStringProperty("remainingTime");
        attemptSection.addStringProperty("name");
        attemptSection.addStringProperty("duration");
        attemptSection.addIntProperty("order");
        attemptSection.addStringProperty("instructions");
        attemptSection.implementsInterface("android.os.Parcelable");
        return attemptSection;
    }

    private static void addSectionToAttempt(Entity attempt, Entity section) {
        Property attemptId = section.addLongProperty("attemptId").getProperty();
        ToMany attemptToSections = attempt.addToMany(section, attemptId);
        attemptToSections.setName("sections");
    }

    private static Entity addCourse(Schema schema) {
        Entity course = schema.addEntity("Course");
        course.addLongProperty("id").primaryKey();
        course.addStringProperty("url");
        course.addStringProperty("title");
        course.addStringProperty("description");
        course.addStringProperty("image");
        course.addStringProperty("modified");
        course.addLongProperty("modifiedDate");
        course.addStringProperty("contentsUrl");
        course.addStringProperty("chaptersUrl");
        course.addStringProperty("slug");
        course.addIntProperty("trophiesCount");
        course.addIntProperty("chaptersCount");
        course.addIntProperty("contentsCount");
        course.addIntProperty("order");
        course.addBooleanProperty("active");
        course.addStringProperty("external_content_link");
        course.addStringProperty("external_link_label");
        course.addBooleanProperty("childItemsLoaded").notNull();
        course.addBooleanProperty("isProduct");
        course.addBooleanProperty("isMyCourse");
        course.addIntProperty("examsCount");
        course.addIntProperty("videosCount");
        course.addIntProperty("htmlContentsCount");
        course.addIntProperty("attachmentsCount");
        course.addStringProperty("expiryDate");
        course.addStringProperty("tags").customType(
                "in.testpress.util.StringList",
                "in.testpress.util.StringListConverter"
        );
        course.addBooleanProperty("allowCustomTestGeneration");
        course.addIntProperty("maxAllowedViewsPerVideo");
        return course;
    }

    private static Entity addChapter(Schema schema) {
        Entity chapter = schema.addEntity("Chapter");
        chapter.addLongProperty("id").primaryKey();
        chapter.addStringProperty("name");
        chapter.addStringProperty("description");
        chapter.addStringProperty("slug");
        chapter.addStringProperty("image");
        chapter.addStringProperty("modified");
        chapter.addLongProperty("modifiedDate");
        chapter.addStringProperty("courseUrl");
        chapter.addStringProperty("contentUrl");
        chapter.addStringProperty("childrenUrl");
        chapter.addStringProperty("parentSlug");
        chapter.addStringProperty("parentUrl");
        chapter.addBooleanProperty("leaf");
        chapter.addStringProperty("url");
        chapter.addIntProperty("requiredTrophyCount");
        chapter.addBooleanProperty("isLocked");
        chapter.addIntProperty("contentsCount");
        chapter.addIntProperty("childrenCount");
        chapter.addBooleanProperty("active");
        return chapter;
    }

    private static Entity addReviewAttempt(Schema schema) {
        Entity attempt = schema.addEntity("ReviewAttempt");
        attempt.addLongProperty("id").primaryKey();
        attempt.addIntProperty("totalQuestions");
        attempt.addStringProperty("score");
        attempt.addStringProperty("rank");
        attempt.addStringProperty("reviewUrl");
        attempt.addIntProperty("correctCount");
        attempt.addIntProperty("incorrectCount");
        attempt.addStringProperty("timeTaken");
        attempt.addStringProperty("percentile");
        attempt.addIntProperty("speed");
        attempt.addIntProperty("accuracy");
        return attempt;
    }

    private static Entity addReviewItem(Schema schema) {
        Entity reviewItem = schema.addEntity("ReviewItem");
        reviewItem.addLongProperty("id").primaryKey();
        reviewItem.addIntProperty("index");
        reviewItem.addStringProperty("url");
        reviewItem.addIntProperty("order");
        reviewItem.addStringProperty("duration");
        reviewItem.addStringProperty("bestDuration");
        reviewItem.addStringProperty("averageDuration");
        reviewItem.addStringProperty("essayText");
        reviewItem.addStringProperty("essayTopic");
        reviewItem.addStringProperty("selectedAnswers").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        ).codeBeforeField("@SerializedName(value=\"selected_answer_ids\", alternate={\"selected_answers\"})");
        reviewItem.addBooleanProperty("review");
        reviewItem.addIntProperty("commentsCount");
        reviewItem.addIntProperty("correctPercentage");
        reviewItem.addLongProperty("bookmarkId");
        reviewItem.addStringProperty("marks");
        reviewItem.addStringProperty("shortText");
        reviewItem.addStringProperty("result");
        return reviewItem;
    }

    private static Entity addSelectedAnswer(Schema schema) {
        Entity selectedAnswer = schema.addEntity("SelectedAnswer");
        selectedAnswer.addIdProperty().autoincrement();
        selectedAnswer.addIntProperty("answerId");
        selectedAnswer.addLongProperty("reviewItemId");
        return selectedAnswer;
    }

    private static Entity addReviewQuestion(Schema schema) {
        Entity reviewQuestion = schema.addEntity("ReviewQuestion");
        reviewQuestion.addLongProperty("id").primaryKey();
        reviewQuestion.addStringProperty("questionHtml");
        reviewQuestion.addStringProperty("direction");
        reviewQuestion.addStringProperty("subject");
        reviewQuestion.addStringProperty("explanationHtml");
        reviewQuestion.addStringProperty("type");
        reviewQuestion.addStringProperty("commentsUrl");
        reviewQuestion.addStringProperty("language");
        reviewQuestion.addFloatProperty("percentageGotCorrect");
        reviewQuestion.addLongProperty("directionId");
        reviewQuestion.addLongProperty("subjectId");
        reviewQuestion.addStringProperty("answerIds").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        reviewQuestion.addStringProperty("translationIds").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        return reviewQuestion;
    }

    private static Entity addReviewQuestionTranslation(Schema schema) {
        Entity reviewQuestionTranslation = schema.addEntity("ReviewQuestionTranslation");
        reviewQuestionTranslation.addIdProperty().autoincrement();
        reviewQuestionTranslation.addStringProperty("questionHtml");
        reviewQuestionTranslation.addStringProperty("direction");
        reviewQuestionTranslation.addStringProperty("explanation");
        reviewQuestionTranslation.addStringProperty("language");
        reviewQuestionTranslation.addLongProperty("directionId");
        reviewQuestionTranslation.addStringProperty("answerIds").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        return reviewQuestionTranslation;
    }


    private static Entity addStream(Schema schema) {
        Entity stream = schema.addEntity("Stream");
        stream.addIdProperty().autoincrement();
        stream.addStringProperty("format");
        stream.addStringProperty("url");
        stream.addStringProperty("hlsUrl");
        stream.addStringProperty("dashUrl");
        return stream;
    }

    private static Entity addReviewAnswer(Schema schema) {
        Entity reviewAnswer = schema.addEntity("ReviewAnswer");
        reviewAnswer.addLongProperty("id").primaryKey();
        reviewAnswer.addStringProperty("textHtml");
        reviewAnswer.addBooleanProperty("isCorrect");
        reviewAnswer.addStringProperty("marks");
        return reviewAnswer;
    }

    private static Entity addReviewAnswerTranslation(Schema schema) {
        Entity reviewAnswerTranslation = schema.addEntity("ReviewAnswerTranslation");
        reviewAnswerTranslation.addLongProperty("id");
        reviewAnswerTranslation.addStringProperty("textHtml");
        reviewAnswerTranslation.addBooleanProperty("isCorrect");
        reviewAnswerTranslation.addStringProperty("marks");
        return reviewAnswerTranslation;
    }

    private static void addStreamToVideo(Entity stream, Entity video) {
        Property streamId = stream.addLongProperty("videoId").getProperty();
        ToMany streamsToVideo = video.addToMany(stream, streamId);
        streamsToVideo.setName("streams");
    }

    private static void addAttemptToReviewItem(Entity reviewAttempt, Entity reviewItem) {
        Property attemptId = reviewItem.addLongProperty("attemptId").getProperty();
        ToMany attemptToReviewItems = reviewAttempt.addToMany(reviewItem, attemptId);
        attemptToReviewItems.setName("reviewItems");
    }

    private static void addReviewItemToQuestion(Entity reviewItem, Entity reviewQuestion) {
        Property questionId = reviewItem.addLongProperty("questionId").getProperty();
        ToOne reviewItemToQuestion = reviewItem.addToOne(reviewQuestion, questionId);
        reviewItemToQuestion.setName("question");
    }

    private static void addReviewQuestionToAnswers(Entity reviewQuestion, Entity reviewAnswer) {
        Property reviewQuestionId = reviewAnswer.addLongProperty("questionId").getProperty();
        ToMany questionToAnswers = reviewQuestion.addToMany(reviewAnswer, reviewQuestionId);
        questionToAnswers.setName("answers");
    }

    private static void addReviewItemToUserUploadedFiles(Entity reviewItem, Entity userUploadedFile) {
        Property reviewUserUploadedFileId = userUploadedFile.addLongProperty("reviewItemId").getProperty();
        ToMany reviewItemToUserUploadedFiles = reviewItem.addToMany(userUploadedFile, reviewUserUploadedFileId);
        reviewItemToUserUploadedFiles.setName("files");
    }

    private static void addTranslationsToReviewQuestion(Entity reviewQuestion, Entity translation) {
        Property reviewQuestionId = translation.addLongProperty("questionId").getProperty();
        ToMany questionToTranslations = reviewQuestion.addToMany(translation, reviewQuestionId);
        questionToTranslations.setName("translations");
    }

    private static void addAnswersToReviewTranslations(Entity questionTranslation, Entity answerTranslation) {
        Property reviewQuestionId = answerTranslation.addLongProperty("questionTranslationId").getProperty();
        ToMany questionToAnswers = questionTranslation.addToMany(answerTranslation, reviewQuestionId);
        questionToAnswers.setName("answers");
    }

    private static void addBookmarkFolder(Schema schema) {
        Entity folder = schema.addEntity("BookmarkFolder");
        folder.addLongProperty("id").primaryKey();
        folder.addStringProperty("name");
        folder.addIntProperty("bookmarksCount");
    }

    private static Entity addBookmark(Schema schema) {
        Entity bookmark = schema.addEntity("Bookmark");
        bookmark.addLongProperty("id").primaryKey();
        bookmark.addStringProperty("folder");
        bookmark.addLongProperty("folderId");
        bookmark.addLongProperty("objectId");
        bookmark.addStringProperty("modified");
        bookmark.addLongProperty("modifiedDate");
        bookmark.addStringProperty("created");
        bookmark.addLongProperty("createdDate");
        bookmark.addBooleanProperty("loadedInAllFolder");
        bookmark.addBooleanProperty("loadedInRespectiveFolder");
        bookmark.addBooleanProperty("active");
        return bookmark;
    }

    private static Entity addContentType(Schema schema) {
        Entity contentType = schema.addEntity("ContentType");
        contentType.addLongProperty("id").primaryKey().autoincrement();
        Property model = contentType.addStringProperty("model").getProperty();
        Property appLabel = contentType.addStringProperty("appLabel").getProperty();

        // Unique together model & app_label
        Index index = new Index();
        index.addProperty(model);
        index.addProperty(appLabel);
        index.makeUnique();

        contentType.addIndex(index);
        return contentType;
    }

    private static void addContentTypeToBookmark(Entity bookmark, Entity contentType) {
        Property contentTypeId = bookmark.addLongProperty("contentTypeId").getProperty();
        bookmark.addToOne(contentType, contentTypeId, "contentType");
    }

    private static Entity addAnswerTranslation(Schema schema) {
        Entity answerTranslation = schema.addEntity("AnswerTranslation");
        answerTranslation.addLongProperty("id").primaryKey();
        answerTranslation.addStringProperty("textHtml");
        answerTranslation.addLongProperty("answerId");
        return answerTranslation;
    }

    private static void addAnswersTranslationToReviewTranslations(Entity questionTranslation,
                                                                  Entity answerTranslation) {

        Property questionId = answerTranslation.addLongProperty("questionId").getProperty();
        ToMany questionToAnswers = questionTranslation.addToMany(answerTranslation, questionId);
        questionToAnswers.setName("answerTranslations");
    }

    private static void addSubject(Schema schema) {
        Entity subject = schema.addEntity("Subject");
        subject.addLongProperty("id").primaryKey();
        subject.addStringProperty("name");
    }

    private static Entity addDirection(Schema schema) {
        Entity direction = schema.addEntity("Direction");
        direction.addLongProperty("id").primaryKey();
        direction.addStringProperty("html");
        return direction;
    }

    private static void addDirectionTranslation(Schema schema) {
        Entity direction = schema.addEntity("DirectionTranslation");
        direction.addLongProperty("id").primaryKey();
        direction.addStringProperty("html");
    }

}
