package com.cts.salesprocessing.services;

import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.salesprocessing.dao.PolicyDetailsDao;
import com.cts.salesprocessing.dao.QuotationDao;
import com.cts.salesprocessing.exception.PolicyNotFoundException;
import com.cts.salesprocessing.exception.QuotationNotFoundException;
import com.cts.salesprocessing.feign.CustomerProxy;
import com.cts.salesprocessing.model.Customer;
import com.cts.salesprocessing.model.CustomerDisplay;
import com.cts.salesprocessing.model.PolicyDetails;
import com.cts.salesprocessing.model.Quotation;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QuotationServiceImpl implements QuotationService {

	@Autowired
	private PolicyDetailsDao policyDao;

	@Autowired
	private QuotationDao quotationDao;

	@Autowired
	private CustomerProxy customerProxy;

	/**
	 * 
	 * -------------------------- Quotation Calculation ----------------
	 * 
	 * @author 841418
	 * 
	 */
	private BiFunction<PolicyDetails, Customer, Quotation> quotationCalculate = (policyIn, customerIn) -> {
		Quotation quote = new Quotation();
		quote.setPAN(customerIn.getPAN());
		quote.setPolicyName(policyIn.getName());
		quote.setSubmitted(false);
		quote.setAmountInsured(policyIn.getInsuranceFactor() * 25000);
		quote.setPremium(quote.getAmountInsured() / (policyIn.getPremiumPeriod() * 12));
		return quote;
	};

	@Override
	public Quotation getQuotation(String policyName, String pAN) throws PolicyNotFoundException {
		// TODO Auto-generated method stub
		log.debug("Service IN");
		PolicyDetails policyDetails = policyDao.findById(policyName)
				.orElseThrow(() -> new PolicyNotFoundException(policyName + " does not exist"));

		Customer customer = customerProxy.getCustomerByPan(pAN).getBody();

		log.debug("Service OUT");
		return quotationCalculate.apply(policyDetails, customer);
	}

	@Override
	public String saveQuotation(Quotation quotation) {
		// TODO Auto-generated method stub
		log.debug("Service IN");
		quotationDao.save(quotation);
		log.debug("Service OUT");
		return "Quotation Saved.";
	}

	@Override
	public Quotation retriveQuotation(int id) throws QuotationNotFoundException {
		// TODO Auto-generated method stub

		log.debug("Service IN");
		Quotation quotation = quotationDao.findById(id)
				.orElseThrow(() -> new QuotationNotFoundException(id + ": is not found."));

		log.debug("Service OUT");
		return quotation;
	}

	@Override
	public String submitApplication(Quotation quotation) {
		// TODO Auto-generated method stub
		log.debug("Service IN");
		quotation.setSubmitted(true);
		quotationDao.save(quotation);
		log.debug("Service OUT");
		return "Form submitted";
	}

	@Override
	public CustomerDisplay getCustomer(String pan) {
		// TODO Auto-generated method stub
		Customer customer = customerProxy.getCustomerByPan(pan).getBody();
		return new CustomerDisplay(customer.getName(), customer.getGender());
	}

}
