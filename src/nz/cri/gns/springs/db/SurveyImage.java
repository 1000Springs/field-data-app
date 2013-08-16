package nz.cri.gns.springs.db;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

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
	
	@DatabaseField private String imageType;
	@DatabaseField private String fileName;


	public Survey getSurvey() {
		return survey;
	}

	public void setSurvey(Survey survey) {
		this.survey = survey;
	}

	public String getImageType() {
		return imageType;
	}
	
	public void setImageType(String imageType) {
		this.imageType = imageType;
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
	
	public static int getImageCount(Survey survey, SpringsDbHelper dbHelper) {
		RuntimeExceptionDao<SurveyImage, Long> dao = dbHelper.getSurveyImageDao();
		try {
			return (int)dao.queryRawValue("select count(*) from SurveyImage where survey_id=?", String.valueOf(survey.getId()));
		} catch (Exception e) {
			Log.e(SurveyImage.class.getSimpleName(), "Error retrieving SurveyImage count", e);
			return 0;
		}		
	}

}
