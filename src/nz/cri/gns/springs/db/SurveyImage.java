package nz.cri.gns.springs.db;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class SurveyImage extends PersistentObject {

	private static final long serialVersionUID = 5806825941895415396L;

	@DatabaseField(foreign = true)
	private Survey survey;
	
	@DatabaseField private ImageType imageType;
	@DatabaseField private String fileName;
	
	public enum ImageType {
		BEST_PHOTO, BEST_SKETCH, ANNOTATED_PHOTO
	}

	public Survey getSurvey() {
		return survey;
	}

	public void setSurvey(Survey survey) {
		this.survey = survey;
	}

	public ImageType getImageType() {
		return imageType;
	}
	
	public void setImageType(ImageType imageType) {
		this.imageType = imageType;
	}

	public void setImageTypeString(String imageType) {
		this.imageType = ImageType.valueOf(imageType);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public static List<SurveyImage> getBySurvey(Survey survey, SpringsDbHelper dbHelper) {

		List<SurveyImage> surveyImageList;
		RuntimeExceptionDao<SurveyImage, Long> dao = dbHelper.getSurveyImageDao();
		QueryBuilder<SurveyImage, Long> queryBuilder = dao.queryBuilder();
		try {
			queryBuilder.where().eq("survey_id", survey.getId());
			PreparedQuery<SurveyImage> preparedQuery = queryBuilder.prepare();
			surveyImageList = dao.query(preparedQuery);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			surveyImageList = new ArrayList<SurveyImage>();
		}
		return surveyImageList;

	}

}
