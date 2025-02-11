package fr.pantheonsorbonne.ufr27.miage.service;

import fr.pantheonsorbonne.ufr27.miage.exception.CannotUpdateLoanException;
import fr.pantheonsorbonne.ufr27.miage.exception.CannotUpdateLoanException.LoanAlreadyProcessedException;
import fr.pantheonsorbonne.ufr27.miage.model.Loan;

import java.util.Collection;

public interface LoaningService {

    Collection<Loan> getAllLoans();

    void acceptLoan(int loanId, String status) throws LoanAlreadyProcessedException, CannotUpdateLoanException.LoanNotFoundException;

    void createLoan(fr.pantheonsorbonne.ufr27.miage.dto.Loan loan);

}
