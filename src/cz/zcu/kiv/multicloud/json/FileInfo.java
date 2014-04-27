package cz.zcu.kiv.multicloud.json;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.multicloud.filesystem.FileType;

public class FileInfo {

	public static final String PATH_SEPARATOR = "/";
	public static final String[] FOLDER_MIME_TYPES = {
		"text/directory",
		"inode/directory",
		"x-directory/normal",
		"resource/folder",
		"application/vnd.google-apps.folder"
	};

	private String id;
	private String name;
	private String path;
	private List<ParentInfo> parents;
	private FileType fileType;
	private String mimeType;
	private long size;
	private List<FileInfo> content;
	private boolean deleted;
	private boolean shared;

	public void fillMissing() {
		if (parents == null) {
			parents = new ArrayList<>();
		}
		if (content == null) {
			content = new ArrayList<>();
		}
		if (path != null) {
			if (name == null && path.contains(PATH_SEPARATOR)) {
				name = path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1);
			}
			if (parents.isEmpty()) {
				if (path.contains(PATH_SEPARATOR)) {
					ParentInfo parent = new ParentInfo();
					parent.setPath(path.substring(0, path.lastIndexOf(PATH_SEPARATOR) + 1));
					parents.add(parent);
				}
			}
		}
		if (fileType == null) {
			if (mimeType != null) {
				for (String folderType: FOLDER_MIME_TYPES)  {
					if (mimeType.equalsIgnoreCase(folderType)) {
						fileType = FileType.FOLDER;
						break;
					}
				}
				if (fileType == null) {
					fileType = FileType.FILE;
				}
			} else {
				fileType = FileType.FOLDER;
			}
		}
	}

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

	public FileType getFileType() {
		return fileType;
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

	public void setParent(String parent) {
		if (parents == null) {
			parents = new ArrayList<>();
		}
		ParentInfo p = new ParentInfo();
		p.setId(parent);
		parents.add(p);
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

	public void setStringFileType(String fileType) {
		this.fileType = FileType.fromString(fileType);
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

}
