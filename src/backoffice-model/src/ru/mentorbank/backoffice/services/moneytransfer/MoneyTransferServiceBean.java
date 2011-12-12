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
	private AccountService accountService;// проверка баланса счёта
	private StopListService stopListService;//
	private OperationDao operationDao;

	public void transfer(TransferRequest request) throws TransferException {
		// Создаём новый экземпляр внутреннего класса, для того, чтобы можно
		// было хранить в состоянии объекта информацию по каждому запросу.
		// Так как MoneyTransferServiceBean конфигурируется как singleton
		// scoped, то в нём нельзя хранить состояние уровня запроса из-за
		// проблем параллельного доступа.
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
			saveOperation();//вначале сохр, потом проводим
			if (isStopListInfoOK()) {
				transferDo();
				removeSuccessfulOperation();
			} else
				throw new TransferException(
						"Невозможно сделать перевод. Необходимо ручное вмешательство.");
		}

		/**
		 * Если операция перевода прошла, то её нужно удалить из таблицы
		 * операций для ручного вмешательства
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
			
		    	// TODO: Необходимо сделать вызов операции saveOperation и сделать
			// соответствующий тест вызова операции operationDao.saveOperation()		

		private void transferDo() throws TransferException {
			// Эту операцию пока не реализовавем. Она должна вызывать
			// CDCMoneyTransferServiceConsumer которого ещё нет
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
/*				почему посылается объект Request, а не сама AccountInfo - сейчас в них информация почти
				одинаковая? Считается чем-то нехорошим обработка информации в StopListService или нехорошо отправлять "на сторону" все данные осчете?
				Что мешает это делать каким-нибудь преобразованием объекта, а не так как сейчас?*/
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
		непонятно где это можно использовать сейчас. Возможно это надо использовать при отправке 
		operation, отправляя операции, требующие ручного вмешательства в определенное место. */

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
