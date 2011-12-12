package ru.mentorbank.backoffice.services.stoplist;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ru.mentorbank.backoffice.model.stoplist.JuridicalStopListRequest;
import ru.mentorbank.backoffice.model.stoplist.PhysicalStopListRequest;
import ru.mentorbank.backoffice.model.stoplist.StopListInfo;
import ru.mentorbank.backoffice.model.stoplist.StopListStatus;
import ru.mentorbank.backoffice.test.AbstractSpringTest;

public class StopListServiceStubTest extends AbstractSpringTest{

	@Autowired
	private StopListServiceStub stopListService;
	private JuridicalStopListRequest stopListRequest;
	private PhysicalStopListRequest stopListRequestPhysical;
	
	@Before
	public void setUp(){		
		stopListRequest = new JuridicalStopListRequest();
		stopListRequestPhysical = new PhysicalStopListRequest();
	}
	
	@Test
	public void getJuridicalStopListInfo_OK(){
		// setup SUT
		stopListRequest.setInn(StopListServiceStub.INN_FOR_OK_STATUS);
		// Call SUT
		StopListInfo info = stopListService.getJuridicalStopListInfo(stopListRequest);
		// Validate SUT
		assertNotNull("Информация должна быть заполнена",info);
		assertEquals(StopListStatus.OK, info.getStatus());
	}
	
	@Test
	public void getJuridicalStopListInfo_STOP(){
		stopListRequest.setInn(StopListServiceStub.INN_FOR_STOP_STATUS);
		StopListInfo info = stopListService.getJuridicalStopListInfo(stopListRequest);
		assertNotNull("Информация должна быть заполнена",info);
		assertEquals(StopListStatus.STOP, info.getStatus());
	}
	
	@Test
	public void getPhysicalStopListInfo_STOP(){
		stopListRequestPhysical.setDocumentSeries(StopListServiceStub.DOC_SERIES_FOR_STOP_STATUS);
		stopListRequestPhysical.setDocumentNumber(StopListServiceStub.DOC_NUMBER_FOR_STOP_STATUS);
		StopListInfo info = stopListService.getPhysicalStopListInfo(stopListRequestPhysical);
		assertNotNull("Информация должна быть заполнена",info);
		assertEquals(StopListStatus.STOP, info.getStatus());
	}
	
	@Test
	public void getPhysicalStopListInfo_OK(){
		stopListRequestPhysical.setDocumentSeries(StopListServiceStub.DOC_SERIES_FOR_OK_STATUS);
		stopListRequestPhysical.setDocumentNumber(StopListServiceStub.DOC_NUMBER_FOR_OK_STATUS);
		StopListInfo info = stopListService.getPhysicalStopListInfo(stopListRequestPhysical);
		assertNotNull("Информация должна быть заполнена",info);
		assertEquals(StopListStatus.OK, info.getStatus());
	}
	
}
