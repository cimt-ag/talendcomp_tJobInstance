package de.cimt.talendcomp.jobinstance.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.benoitduffez.ScriptRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.cimt.talendcomp.jobinstance.manage.JobInstanceHelper;

public class JobInstanceHelperTest {
	
	private Map<String, Object> globalMap = new HashMap<>();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public void createConnection() throws Exception {
		String host = System.getProperty("POSTGRES_HOST");
		String port = System.getProperty("POSTGRES_PORT");

		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/postgres", "postgres", "postgres");
		globalMap.put("connection", conn);
	}

	@Before
	public void setup() throws Exception {
		createConnection();
		Connection conn = (Connection) globalMap.get("connection");

		ScriptRunner runner = new ScriptRunner(conn, false, false);
		String file = "scripts/postgresql.sql";

		runner.runScript(new BufferedReader(new FileReader(file)));
	}
	
	@After
	public void tearDown() throws Exception {
		Connection conn = (Connection) globalMap.get("connection");
		if (conn != null && conn.isClosed() == false) {
			conn.close();
		}
	}

	@Test
	public void testCreateAndUpdateEntry() throws Exception {
		// create entry
		String guid = String.valueOf(System.currentTimeMillis());
		String jobName = "JobInstanceHelperTest";
		String expectedWorkItem = "workitem_" + guid;
		String expectedExtJobItem = "extid_" + guid;
		Date expectedTimeRangeEnd = new Date();
		String resultItem = "resultItem_" + guid;
		String schema = "dwh_manage";
		long expectedJobInstanceId = 0;
		{
			createConnection();
			System.out.println("Run 1: Instantiate Helper...");
			JobInstanceHelper helper = new JobInstanceHelper();
			helper.setConnection((Connection) globalMap.get("connection"));
			helper.setUseGeneratedKeys(true);
			helper.setAutoIncrementColumn(false);
//			helper.setSequenceExpression("nextval('" + schema + ".seq_job_instance_id')");
			helper.setJobName(jobName);
			helper.setJobGuid(guid);
			helper.setJobStartedAt(System.currentTimeMillis());
			helper.setSchemaName(schema);
			helper.setJobWorkItem(expectedWorkItem, true);
			helper.setExtJobId(expectedExtJobItem);
			System.out.println("Create entry...");
			helper.createEntry();
			expectedJobInstanceId = helper.getJobInstanceId();
			// check if we get a valid job_instance_id
			assertTrue("JobInstanceId is not greater 0", expectedJobInstanceId > 0l);
			
			System.out.println("Run 1: Update entry...");
			// update entry
			helper.setTimeRangeEnd(expectedTimeRangeEnd);
			helper.setJobResult(resultItem);
			helper.setReturnCode(1);
			helper.updateEntry();
			helper.closeConnection();
		}
		{
			guid = String.valueOf(System.currentTimeMillis() + "x");
			createConnection();
			System.out.println("Run 2: Instantiate Helper...");
			JobInstanceHelper tJobInstanceStart_1 = new JobInstanceHelper();
			tJobInstanceStart_1.setConnection((Connection) globalMap.get("connection"));
			tJobInstanceStart_1.setAutoIncrementColumn(false);
			tJobInstanceStart_1.setSequenceExpression("nextval('dwh_manage.seq_job_instance_id')");
			tJobInstanceStart_1.setJobName(jobName);
			tJobInstanceStart_1.setJobStartedAt(System.currentTimeMillis());
			tJobInstanceStart_1.setJobGuid(guid);
			tJobInstanceStart_1.setSchemaName(schema);
			tJobInstanceStart_1.setJobWorkItem(expectedWorkItem, true);
			tJobInstanceStart_1.setExtJobId(expectedExtJobItem);
			System.out.println("Create entry...");
			tJobInstanceStart_1.createEntry();
			long jobInstanceId = tJobInstanceStart_1.getJobInstanceId();
			// check if we get a valid job_instance_id
			assertTrue("JobInstanceId is not greater 0", jobInstanceId > 0);
			boolean tJobInstanceStart_1_PrevJobExists = tJobInstanceStart_1
					.retrievePreviousInstanceData(true, // onlySuccessful
							true, // onlyOutputData
							false, // only with input data
							false,
							false); // forWorkItem
			globalMap.put("tJobInstanceStart_1_PREV_JOB_EXISTS",
					tJobInstanceStart_1_PrevJobExists);
			if (tJobInstanceStart_1_PrevJobExists) {
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_INSTANCE_ID",
						tJobInstanceStart_1.getPrevJobInstanceId());
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_START_DATE",
						tJobInstanceStart_1.getPrevJobStartDate());
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_STOP_DATE",
						tJobInstanceStart_1.getPrevJobStopDate());
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_TALEND_PID",
						tJobInstanceStart_1.getPrevJobGuid());
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_HOST_PID",
						tJobInstanceStart_1.getPrevHostPid());
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_HOST_NAME",
						tJobInstanceStart_1.getPrevHostName());
				globalMap
						.put("tJobInstanceStart_1_PREV_TIME_RANGE_START",
								tJobInstanceStart_1
										.getPrevTimeRangeStart());
				java.util.Date prevTimeRangeEnd = tJobInstanceStart_1
						.getPrevTimeRangeEnd();
				assertEquals(expectedTimeRangeEnd, prevTimeRangeEnd);
				if (prevTimeRangeEnd == null) {
					prevTimeRangeEnd = sdf.parse("1970-01-01");
				}
				globalMap.put(
						"tJobInstanceStart_1_PREV_TIME_RANGE_END",
						prevTimeRangeEnd);
				globalMap
						.put("tJobInstanceStart_1_PREV_VALUE_RANGE_START",
								tJobInstanceStart_1
										.getPrevValueRangeStart());
				String prevValueRangeEnd = tJobInstanceStart_1
						.getPrevValueRangeEnd();
				if (prevValueRangeEnd == null) {
					prevValueRangeEnd = "0";
				}
				globalMap.put(
						"tJobInstanceStart_1_PREV_VALUE_RANGE_END",
						prevValueRangeEnd);
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_RETURN_CODE",
						tJobInstanceStart_1.getPrevReturnCode());
				globalMap.put("tJobInstanceStart_1_PREV_WORK_ITEM",
						tJobInstanceStart_1.getPrevWorkItem());
				String prevJobResult = tJobInstanceStart_1
						.getPrevJobResult();
				if (prevJobResult == null) {
					prevJobResult = "0";
				}
				globalMap.put(
						"tJobInstanceStart_1_PREV_RESULT_ITEM",
						prevJobResult);
				globalMap.put(
						"tJobInstanceStart_1_PREV_COUNT_INPUT",
						tJobInstanceStart_1.getPrevInput());
				globalMap.put(
						"tJobInstanceStart_1_PREV_COUNT_OUTPUT",
						tJobInstanceStart_1.getPrevOutput());
				globalMap.put(
						"tJobInstanceStart_1_PREV_COUNT_UPDATED",
						tJobInstanceStart_1.getPrevUpdated());
				globalMap.put(
						"tJobInstanceStart_1_PREV_COUNT_REJECTED",
						tJobInstanceStart_1.getPrevRejects());
				globalMap.put(
						"tJobInstanceStart_1_PREV_COUNT_DELETED",
						tJobInstanceStart_1.getPrevDeleted());
			} else {
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_INSTANCE_ID",
						0l);
				globalMap.put(
						"tJobInstanceStart_1_PREV_JOB_START_DATE",sdf.parse("1970-01-01"));
				globalMap
						.remove("tJobInstanceStart_1_PREV_JOB_STOP_DATE");
				globalMap
						.remove("tJobInstanceStart_1_PREV_JOB_TALEND_PID");
				globalMap
						.remove("tJobInstanceStart_1_PREV_JOB_HOST_PID");
				globalMap
						.remove("tJobInstanceStart_1_PREV_JOB_HOST_NAME");
				globalMap
						.remove("tJobInstanceStart_1_PREV_TIME_RANGE_START");
				globalMap.put(
						"tJobInstanceStart_1_PREV_TIME_RANGE_END",sdf.parse("1970-01-01"));
				globalMap
						.remove("tJobInstanceStart_1_PREV_VALUE_RANGE_START");
				globalMap.put(
						"tJobInstanceStart_1_PREV_VALUE_RANGE_END",
						"0");
				globalMap
						.remove("tJobInstanceStart_1_PREV_JOB_RETURN_CODE");
				globalMap
						.remove("tJobInstanceStart_1_PREV_WORK_ITEM");
				globalMap
						.put("tJobInstanceStart_1_PREV_RESULT_ITEM",
								"0");
				globalMap
						.remove("tJobInstanceStart_1_PREV_COUNT_INPUT");
				globalMap
						.remove("tJobInstanceStart_1_PREV_COUNT_OUTPUT");
				globalMap
						.remove("tJobInstanceStart_1_PREV_COUNT_UPDATED");
				globalMap
						.remove("tJobInstanceStart_1_PREV_COUNT_REJECTED");
				globalMap
						.remove("tJobInstanceStart_1_PREV_COUNT_DELETED");
			}
			
			System.out.println("Run 2: Update entry...");
			// update entry
			tJobInstanceStart_1.setTimeRangeEnd(expectedTimeRangeEnd);
			tJobInstanceStart_1.setJobResult(resultItem);
			tJobInstanceStart_1.setReturnCode(1);
			tJobInstanceStart_1.updateEntry();
			tJobInstanceStart_1.closeConnection();
		}
	}
	
}
