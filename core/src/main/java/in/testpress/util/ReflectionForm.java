package in.testpress.util;

public class ReflectionForm {
    private Long id;
    private Boolean submissionMandatory;

    public ReflectionForm() {}

    public ReflectionForm(Long id, Boolean submissionMandatory) {
        this.id = id;
        this.submissionMandatory = submissionMandatory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSubmissionMandatory() {
        return submissionMandatory;
    }

    public void setSubmissionMandatory(Boolean submissionMandatory) {
        this.submissionMandatory = submissionMandatory;
    }
}

