package nz.cri.gns.springs.db;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;


public abstract class PersistentObject implements Serializable {
	
	private static final long serialVersionUID = 4338757173717410713L;

	@DatabaseField(generatedId = true) 
	protected Long id;
	
	@DatabaseField protected Long createdDate = System.currentTimeMillis();
	@DatabaseField protected Long updatedDate = System.currentTimeMillis();
	@DatabaseField protected Status status = Status.NEW;
	
	/**
	 * NEW: object has not been exported since creation
	 * EXPORTED: object has been exported, but has not been updated since last export
	 * UPDATE: object has been updated since last export
	 * @author duncanw
	 */
	public enum Status {
		NEW, UPDATED, EXPORTED
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Long createdDate) {
		this.createdDate = createdDate;
	}

	public Long getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Long updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	};

}
