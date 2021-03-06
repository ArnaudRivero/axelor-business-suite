/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2015 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveToolService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected MoveLineService moveLineService;
	protected MoveLineRepository moveLineRepository;
	protected AccountCustomerService accountCustomerService;
	protected AccountConfigService accountConfigService;
	protected GeneralService generalService;

	@Inject
	public MoveToolService(GeneralService generalService, MoveLineService moveLineService, MoveLineRepository moveLineRepository, 
			AccountCustomerService accountCustomerService, AccountConfigService accountConfigService) {

		this.generalService = generalService;
		this.moveLineService = moveLineService;
		this.moveLineRepository = moveLineRepository;
		this.accountCustomerService = accountCustomerService;
		this.accountConfigService = accountConfigService;

	}


	public boolean isMinus(Invoice invoice)  {
		// Si le montant est négatif, alors on doit inverser le signe du montant
		if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
			return true;
		}
		else  {
			return false;
		}
	}


	public boolean toDoConsolidate()  {
		return generalService.getGeneral().getIsInvoiceMoveConsolidated();
	}


	/**
	 *
	 * @param invoice
	 *
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public boolean isDebitCustomer(Invoice invoice) throws AxelorException  {
		boolean isDebitCustomer;

		switch(invoice.getOperationTypeSelect())  {
		case 1:
			isDebitCustomer = false;
			break;
		case 2:
			isDebitCustomer = true;
			break;
		case 3:
			isDebitCustomer = true;
			break;
		case 4:
			isDebitCustomer = false;
			break;

		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_1), invoice.getInvoiceId()), IException.MISSING_FIELD);
		}

		// Si le montant est négatif, alors on inverse le sens
		if(this.isMinus(invoice))  {
			isDebitCustomer = !isDebitCustomer;
		}

		return isDebitCustomer;
	}



	/**
	 * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte client) de la facture
	 * Récupération par boucle. A privilégié si les lignes d'écriture sont déjà managées par JPA ou si le nombre de lignes
	 * d'écriture n'est pas important (< 100).
	 * @param invoice
	 * 			Une facture
	 * @return
	 * @throws AxelorException
	 */
	public MoveLine getInvoiceCustomerMoveLineByLoop(Invoice invoice) throws AxelorException  {
		if(this.isDebitCustomer(invoice))  {
			return moveLineService.getDebitCustomerMoveLine(invoice);
		}
		else  {
			return moveLineService.getCreditCustomerMoveLine(invoice);
		}
	}


	/**
	 * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte client) de la facture
	 * Récupération par requête. A privilégié si les lignes d'écritures ne sont pas managées par JPA ou si le nombre
	 * d'écriture est très important (> 100)
	 * @param invoice
	 * 			Une facture
	 * @return
	 * @throws AxelorException
	 */
	public MoveLine getInvoiceCustomerMoveLineByQuery(Invoice invoice) throws AxelorException  {

		if(this.isDebitCustomer(invoice))  {
			return moveLineRepository.all().filter("self.move = ?1 AND self.account = ?2 AND self.debit > 0 AND self.amountRemaining > 0",
					invoice.getMove(), invoice.getPartnerAccount()).fetchOne();
		}
		else  {
			return moveLineRepository.all().filter("self.move = ?1 AND self.account = ?2 AND self.credit > 0 AND self.amountRemaining > 0",
				invoice.getMove(), invoice.getPartnerAccount()).fetchOne();
		}
	}


//	public MoveLine getCustomerMoveLineByQuerySum(Invoice invoice) throws AxelorException  {
//
//		if(this.isDebitCustomer(invoice))  {
//			JPA.
//			return MoveLine.all().filter("self.move = ?1 AND self.account = ?2 AND self.debit > 0 AND self.amountRemaining > 0",
//					invoice.getMove(), invoice.getPartnerAccount()).fetchOne();
//		}
//		else  {
//			return MoveLine.all().filter("self.move = ?1 AND self.account = ?2 AND self.credit > 0 AND self.amountRemaining > 0",
//				invoice.getMove(), invoice.getPartnerAccount()).fetchOne();
//		}
//	}


	/**
	 * Fonction permettant de récuperer la ligne d'écriture (en débit et non complétement payée sur le compte client) de la facture ou du rejet de facture
	 * Récupération par boucle. A privilégié si les lignes d'écriture sont déjà managées par JPA ou si le nombre de lignes
	 * d'écriture n'est pas important (< 100).
	 *
	 * @param invoice
	 * 			Une facture
	 * @param isInvoiceReject
	 * 			La facture est-elle rejetée?
	 * @return
	 * @throws AxelorException
	 */
	public MoveLine getCustomerMoveLineByLoop(Invoice invoice) throws AxelorException  {
		if(invoice.getRejectMoveLine() != null && invoice.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
			return invoice.getRejectMoveLine();
		}
		else  {
			return this.getInvoiceCustomerMoveLineByLoop(invoice);
		}
	}


	/**
	 * Fonction permettant de récuperer la ligne d'écriture (en débit et non complétement payée sur le compte client) de la facture ou du rejet de facture
	 * Récupération par requête. A privilégié si les lignes d'écritures ne sont pas managées par JPA ou si le nombre
	 * d'écriture est très important (> 100)
	 *
	 * @param invoice
	 * 			Une facture
	 * @param isInvoiceReject
	 * 			La facture est-elle rejetée?
	 * @return
	 * @throws AxelorException
	 */
	public MoveLine getCustomerMoveLineByQuery(Invoice invoice) throws AxelorException  {
		if(invoice.getRejectMoveLine() != null && invoice.getRejectMoveLine().getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
			return invoice.getRejectMoveLine();
		}
		else  {
			return this.getInvoiceCustomerMoveLineByQuery(invoice);
		}
	}




	public Account getCustomerAccount(Partner partner, Company company, boolean isSupplierAccount) throws AxelorException  {

		AccountingSituation accountingSituation = accountCustomerService.getAccountingSituationService().getAccountingSituation(partner, company);

		if(accountingSituation != null)  {

			if(!isSupplierAccount && accountingSituation.getCustomerAccount() != null )  {
				return accountingSituation.getCustomerAccount();
			}
			else if(isSupplierAccount && accountingSituation.getSupplierAccount() != null)  {
				return accountingSituation.getSupplierAccount();
			}
		}

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		if(isSupplierAccount)  {
			return accountConfigService.getSupplierAccount(accountConfig);
		}
		else  {
			return accountConfigService.getCustomerAccount(accountConfig);
		}

	}





	/**
	 * Fonction permettant de savoir si toutes les lignes d'écritures utilise le même compte que celui passé en paramètre
	 * @param moveLineList
	 * 		Une liste de lignes d'écritures
	 * @param account
	 * 		Le compte que l'on souhaite tester
	 * @return
	 */
	public boolean isSameAccount(List<MoveLine> moveLineList, Account account)  {
		for(MoveLine moveLine : moveLineList)  {
			if(!moveLine.getAccount().equals(account))  {
				return false;
			}
		}
		return true;
	}


	/**
	 * Fonction calculant le restant à utiliser total d'une liste de ligne d'écriture au credit
	 * @param creditMoveLineList
	 * 			Une liste de ligne d'écriture au credit
	 * @return
	 */
	public BigDecimal getTotalCreditAmount(List<MoveLine> creditMoveLineList)  {
		BigDecimal totalCredit = BigDecimal.ZERO;
		for(MoveLine moveLine : creditMoveLineList)  {
			totalCredit = totalCredit.add(moveLine.getAmountRemaining());
		}
		return totalCredit;
	}

	/**
	 * Fonction calculant le restant à utiliser total d'une liste de ligne d'écriture au credit
	 * @param creditMoveLineList
	 * 			Une liste de ligne d'écriture au credit
	 * @return
	 */
	public BigDecimal getTotalDebitAmount(List<MoveLine> debitMoveLineList)  {
		BigDecimal totalDebit = BigDecimal.ZERO;
		for(MoveLine moveLine : debitMoveLineList)  {
			totalDebit = totalDebit.add(moveLine.getAmountRemaining());
		}
		return totalDebit;
	}



	public MoveLine getOrignalInvoiceFromRefund(Invoice invoice)  {

		Invoice originalInvoice = invoice.getOriginalInvoice();

		if(originalInvoice != null && originalInvoice.getMove() != null)  {
			for(MoveLine moveLine : originalInvoice.getMove().getMoveLineList())  {
				if(moveLine.getAccount().getReconcileOk() && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0
						&& moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
					return moveLine;
				}
			}
		}

		return null;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BigDecimal getInTaxTotalRemaining(Invoice invoice) throws AxelorException{
		BigDecimal inTaxTotalRemaining = BigDecimal.ZERO;

		log.debug("Update Remaining amount of invoice : {}", invoice.getInvoiceId());

		if(invoice!=null)  {

			boolean isMinus = this.isMinus(invoice);

			log.debug("Methode 1 : debut"); //TODO
			Beans.get(InvoiceRepository.class).save(invoice);
			log.debug("Methode 1 : milieu");
			MoveLine moveLine = this.getCustomerMoveLineByLoop(invoice);
			log.debug("Methode 1 : fin");

			log.debug("Methode 2 : debut");
//			MoveLine moveLine2 = this.getCustomerMoveLineByQuery(invoice);
			log.debug("Methode 2 : fin");

			if(moveLine != null)  {
				inTaxTotalRemaining = inTaxTotalRemaining.add(moveLine.getAmountRemaining());

				if(isMinus)  {
					inTaxTotalRemaining = inTaxTotalRemaining.negate();
				}
			}
		}
		return inTaxTotalRemaining;
	}


	/**
	 * Methode permettant de récupérer la contrepartie d'une ligne d'écriture
	 * @param moveLine
	 * 			Une ligne d'écriture
	 * @return
	 */
	public MoveLine getOppositeMoveLine(MoveLine moveLine)  {
		if(moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  {
			for(MoveLine oppositeMoveLine : moveLine.getMove().getMoveLineList())  {
				if(oppositeMoveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)  {
					return oppositeMoveLine;
				}
			}
		}
		if(moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)  {
			for(MoveLine oppositeMoveLine : moveLine.getMove().getMoveLineList())  {
				if(oppositeMoveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)  {
					return oppositeMoveLine;
				}
			}
		}
		return null;
	}

	
	public List <MoveLine> orderListByDate(List <MoveLine> list)  {
		Collections.sort(list, new Comparator<MoveLine>() {

			@Override
			public int compare(MoveLine o1, MoveLine o2) {
				
				return o1.getDate().compareTo(o2.getDate());
			}
		});

	return list;
}
		
}