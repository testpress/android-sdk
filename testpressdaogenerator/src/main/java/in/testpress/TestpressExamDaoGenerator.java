package in.testpress;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;

import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.ToMany;
import org.greenrobot.greendao.generator.ToOne;

public class TestpressExamDaoGenerator {
    // Increase the version if any modification has been made in this file.
    private static final int VERSION = 5;

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(VERSION, "in.testpress.exam.models.greendao");

        Entity attempt = addAttempt(schema);
        Entity reviewItem = addReviewItem(schema);
        Entity reviewQuestion = addReviewQuestion(schema);
        Entity reviewAnswer = addReviewAnswer(schema);
        Entity reviewQuestionTranslation = addReviewQuestionTranslation(schema);
        Entity reviewAnswerTranslation = addReviewAnswerTranslation(schema);
        addSelectedAnswer(schema);
        addAttemptToReviewItem(attempt, reviewItem);
        addReviewItemToQuestion(reviewItem, reviewQuestion);
        addReviewQuestionToAnswers(reviewQuestion, reviewAnswer);
        addTranslationsToReviewQuestion(reviewQuestion, reviewQuestionTranslation);
        addAnswersToReviewTranslations(reviewQuestionTranslation, reviewAnswerTranslation);

        new DaoGenerator().generateAll(schema, "exam/src/main/java");
    }

    private static Entity addAttempt(Schema schema) {
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
        reviewItem.addStringProperty("selectedAnswers").customType(
                "List<Integer>",
                "IntegerListConverter"
        );
        reviewItem.addBooleanProperty("review");
        reviewItem.addIntProperty("commentsCount");
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
        reviewQuestion.addStringProperty("commentsUrl");
        reviewQuestion.addStringProperty("language");
        return reviewQuestion;
    }

    private static Entity addReviewQuestionTranslation(Schema schema) {
        Entity reviewQuestionTranslation = schema.addEntity("ReviewQuestionTranslation");
        reviewQuestionTranslation.addIdProperty().autoincrement();
        reviewQuestionTranslation.addStringProperty("questionHtml");
        reviewQuestionTranslation.addStringProperty("direction");
        reviewQuestionTranslation.addStringProperty("explanationHtml");
        reviewQuestionTranslation.addStringProperty("language");
        return reviewQuestionTranslation;
    }

    private static Entity addReviewAnswer(Schema schema) {
        Entity reviewAnswer = schema.addEntity("ReviewAnswer");
        reviewAnswer.addLongProperty("id").primaryKey();
        reviewAnswer.addStringProperty("textHtml");
        reviewAnswer.addBooleanProperty("isCorrect");
        return reviewAnswer;
    }

    private static Entity addReviewAnswerTranslation(Schema schema) {
        Entity reviewAnswerTranslation = schema.addEntity("ReviewAnswerTranslation");
        reviewAnswerTranslation.addLongProperty("translationAnswerId").primaryKey().autoincrement();
        reviewAnswerTranslation.addLongProperty("id");
        reviewAnswerTranslation.addStringProperty("textHtml");
        reviewAnswerTranslation.addBooleanProperty("isCorrect");
        return reviewAnswerTranslation;
    }

    private static void addAttemptToReviewItem(Entity attempt, Entity reviewItem) {
        Property attemptId = reviewItem.addLongProperty("attemptId").getProperty();
        ToMany attemptToReviewItems = attempt.addToMany(reviewItem, attemptId);
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
}
