package in.testpress.store.models;

import com.google.gson.annotations.SerializedName;

public class CoursesItem{

	@SerializedName("image")
	private String image;

	@SerializedName("exams_count")
	private int examsCount;

	@SerializedName("created")
	private String created;

	@SerializedName("description")
	private String description;

	@SerializedName("title")
	private String title;

	@SerializedName("chapters_count")
	private int chaptersCount;

	@SerializedName("device_access_control")
	private String deviceAccessControl;

	@SerializedName("created_by")
	private int createdBy;

	@SerializedName("enable_discussions")
	private boolean enableDiscussions;

	@SerializedName("url")
	private String url;

	@SerializedName("contents_count")
	private int contentsCount;

	@SerializedName("contents_url")
	private String contentsUrl;

	@SerializedName("chapters_url")
	private String chaptersUrl;

	@SerializedName("modified")
	private String modified;

	@SerializedName("videos_count")
	private int videosCount;

	@SerializedName("external_content_link")
	private String externalContentLink;

	@SerializedName("id")
	private int id;

	@SerializedName("attachments_count")
	private int attachmentsCount;

	@SerializedName("slug")
	private String slug;

	@SerializedName("html_contents_count")
	private int htmlContentsCount;

	@SerializedName("order")
	private int order;

	@SerializedName("external_link_label")
	private String externalLinkLabel;

	public String getImage(){
		return image;
	}

	public int getExamsCount(){
		return examsCount;
	}

	public String getCreated(){
		return created;
	}

	public String getDescription(){
		return description;
	}

	public String getTitle(){
		return title;
	}

	public int getChaptersCount(){
		return chaptersCount;
	}

	public String getDeviceAccessControl(){
		return deviceAccessControl;
	}

	public int getCreatedBy(){
		return createdBy;
	}

	public boolean isEnableDiscussions(){
		return enableDiscussions;
	}

	public String getUrl(){
		return url;
	}

	public int getContentsCount(){
		return contentsCount;
	}

	public String getContentsUrl(){
		return contentsUrl;
	}

	public String getChaptersUrl(){
		return chaptersUrl;
	}

	public String getModified(){
		return modified;
	}

	public int getVideosCount(){
		return videosCount;
	}

	public String getExternalContentLink(){
		return externalContentLink;
	}

	public int getId(){
		return id;
	}

	public int getAttachmentsCount(){
		return attachmentsCount;
	}

	public String getSlug(){
		return slug;
	}

	public int getHtmlContentsCount(){
		return htmlContentsCount;
	}

	public int getOrder(){
		return order;
	}

	public String getExternalLinkLabel(){
		return externalLinkLabel;
	}
}