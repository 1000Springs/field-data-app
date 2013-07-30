package nz.cri.gns.springs.db;


public class PersistentObject {
	
	private long id;
	private long createdDate = System.currentTimeMillis();
	private long updatedDate = System.currentTimeMillis();
	private String status = Status.NEW.toString();
	
	public enum Status {
		NEW, UPDATED, EXPORTED
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	public long getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(long updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	};
	
	public void setStatus(Status status) {
		this.status = status.toString();
	};

}
