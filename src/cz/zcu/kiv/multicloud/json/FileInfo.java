package cz.zcu.kiv.multicloud.json;

import java.util.List;

import cz.zcu.kiv.multicloud.filesystem.FileType;

public class FileInfo {

	private String id;
	private String name;
	private String path;
	private List<ParentInfo> parents;
	private FileType type;
	private String mimeType;
	private long size;
	private List<FileInfo> content;
	private boolean deleted;
	private boolean shared;

	public List<FileInfo> getContent() {
		return content;
	}

	public String getId() {
		return id;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getName() {
		return name;
	}

	public List<ParentInfo> getParents() {
		return parents;
	}

	public String getPath() {
		return path;
	}

	public long getSize() {
		return size;
	}

	public FileType getType() {
		return type;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isShared() {
		return shared;
	}

	public void setContent(List<FileInfo> content) {
		this.content = content;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParents(List<ParentInfo> parents) {
		this.parents = parents;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setType(FileType type) {
		this.type = type;
	}

}
