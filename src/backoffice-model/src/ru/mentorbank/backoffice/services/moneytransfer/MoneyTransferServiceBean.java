package ru.mentorbank.backoffice.services.moneytransfer;

import ru.mentorbank.backoffice.dao.OperationDao;
import ru.mentorbank.backoffice.dao.exception.OperationDaoException;
import ru.mentorbank.backoffice.model.Account;
import ru.mentorbank.backoffice.model.Operation;
import ru.mentorbank.backoffice.model.stoplist.JuridicalStopListRequest;
import ru.mentorbank.backoffice.model.stoplist.PhysicalStopListRequest;
import ru.mentorbank.backoffice.model.stoplist.StopListInfo;
import ru.mentorbank.backoffice.model.stoplist.StopListStatus;
import ru.mentorbank.backoffice.model.transfer.AccountInfo;
import ru.mentorbank.backoffice.model.transfer.JuridicalAccountInfo;
import ru.mentorbank.backoffice.model.transfer.PhysicalAccountInfo;
import ru.mentorbank.backoffice.model.transfer.TransferRequest;
import ru.mentorbank.backoffice.services.accounts.AccountService;
import ru.mentorbank.backoffice.services.moneytransfer.exceptions.TransferException;
import ru.mentorbank.backoffice.services.stoplist.StopListService;
//import java.util.Date;
import java.util.GregorianCalendar;


public class MoneyTransferServiceBean implements MoneyTransferService {

	public static final String LOW_BALANCE_ERROR_MESSAGE = "Can not transfer money, because of low balance in the source account";
	private AccountService accountService;// �������� ������� �����
	private StopListService stopListService;//
	private OperationDao operationDao;

	public void transfer(TransferRequest request) throws TransferException {
		// ������ ����� ��������� ����������� ������, ��� ����, ����� �����
		// ���� ������� � ��������� ������� ���������� �� ������� �������.
		// ��� ��� MoneyTransferServiceBean ��������������� ��� singleton
		// scoped, �� � �� ������ ������� ��������� ������ ������� ��-��
		// ������� ������������� �������.
		new MoneyTransfer(request).transfer();
	}

	class MoneyTransfer {   
	    
		public MoneyTransfer(TransferRequest request) {
			this.request = request;
		}
		
		private TransferRequest request;
		private StopListInfo srcStopListInfo;
		private StopListInfo dstStopListInfo;

		public void transfer() throws TransferException {
			verifySrcBalance();
			initializeStopListInfo();
			saveOperation();//������� ����, ����� ��������
			if (isStopListInfoOK()) {
				transferDo();
				removeSuccessfulOperation();
			} else
				throw new TransferException(
						"���������� ������� �������. ���������� ������ �������������.");
		}

		/**
		 * ���� �������� �������� ������, �� � ����� ������� �� �������
		 * �������� ��� ������� �������������
		 */
		private void removeSuccessfulOperation() {

		}

		

		private void saveOperation() throws TransferException {
		    	Operation operation = new Operation();
		    	
		    	operation.setCreateDate(new GregorianCalendar());
		    	operation.setDstStoplistInfo(this.dstStopListInfo);
		    	operation.setSrcStoplistInfo(this.srcStopListInfo); 
		    	Account dstAccount = new Account();
		    	dstAccount.setAccountNumber(request.getDstAccount().getAccountNumber());
		    	operation.setDstAccount(dstAccount);
		    	Account srcAccount = new Account();
		    	srcAccount.setAccountNumber(request.getSrcAccount().getAccountNumber()); 
		    	operation.setSrcAccount(srcAccount);

		    	try{
				operationDao.saveOperation(operation);
				} catch(OperationDaoException e){
					e.printStackTrace();
					throw new TransferException("Error: Save operation: " + e.getMessage());
				}
			}
			
		    	// TODO: ���������� ������� ����� �������� saveOperation � �������
			// ��������������� ���� ������ �������� operationDao.saveOperation()		

		private void transferDo() throws TransferException {
			// ��� �������� ���� �� ������������. ��� ������ ��������
			// CDCMoneyTransferServiceConsumer �������� ��� ���
		}
		
		private void initializeStopListInfo() {
			srcStopListInfo = getStopListInfo(request.getSrcAccount());
			dstStopListInfo = getStopListInfo(request.getDstAccount());
		}

		private boolean isStopListInfoOK() {
			if (StopListStatus.OK.equals(srcStopListInfo.getStatus())
					&& StopListStatus.OK.equals(dstStopListInfo.getStatus())) {
				return true;
			}
			return false;
		}

		private StopListInfo getStopListInfo(AccountInfo accountInfo) {
			if (accountInfo instanceof JuridicalAccountInfo) {
				JuridicalAccountInfo juridicalAccountInfo = (JuridicalAccountInfo) accountInfo;
				JuridicalStopListRequest request = new JuridicalStopListRequest();
				request.setInn(juridicalAccountInfo.getInn());
				StopListInfo stopListInfo = stopListService.getJuridicalStopListInfo(request);
/*				������ ���������� ������ Request, � �� ���� AccountInfo - ������ � ��� ���������� �����
				����������? ��������� ���-�� ��������� ��������� ���������� � StopListService ��� �������� ���������� "�� �������" ��� ������ ������?
				��� ������ ��� ������ �����-������ ��������������� �������, � �� ��� ��� ������?*/
				return stopListInfo;
				
			} else if (accountInfo instanceof PhysicalAccountInfo) {
			    	PhysicalAccountInfo physicalAccountInfo = (PhysicalAccountInfo) accountInfo;
			    	PhysicalStopListRequest request = new PhysicalStopListRequest();
				request.setDocumentNumber(physicalAccountInfo.getDocumentNumber());
				request.setDocumentSeries(physicalAccountInfo.getDocumentSeries());
				request.setFirstname(physicalAccountInfo.getFirstname());
				request.setLastname(physicalAccountInfo.getLastname());
				request.setMiddlename(physicalAccountInfo.getMiddlename());
				StopListInfo stopListInfo = stopListService.getPhysicalStopListInfo(request);
				return stopListInfo;
			}
			return null;
		}

	/*	private boolean processStopListStatus(StopListInfo stopListInfo)
				throws TransferException {
			if (StopListStatus.ASKSECURITY.equals(stopListInfo.getStatus())) {
				return false;
			}
			return true;
		} 
		��������� ��� ��� ����� ������������ ������. �������� ��� ���� ������������ ��� �������� 
		operation, ��������� ��������, ��������� ������� ������������� � ������������ �����. */

		private void verifySrcBalance() throws TransferException {
			if (!accountService.verifyBalance(request.getDstAccount()))
				throw new TransferException(LOW_BALANCE_ERROR_MESSAGE);
		}
	}

	public void setAccountService(AccountService accountService) {
		this.accountService = accountService;

	}

	public void setStopListService(StopListService stopListService) {
		this.stopListService = stopListService;
	}

	public void setOperationDao(OperationDao operationDao) {
		this.operationDao = operationDao;
	}
}
