module BankSystem {
  enum Currency { PLN, GBP, USD, CHF, EUR };
  enum AccountType { STANDARD, PREMIUM };

  struct Password { string value; };
  struct Pesel { long value; };
  struct Income { double value; };
  struct Name { string value; };
  struct Surname { string value; };
  struct RepaymentTime { string value; };
  struct AccountCreated { Password password; AccountType accountType; };
  struct CreditData { Income originCurrency; Income foreignCurrency; };

  exception InvalidCredentialsException {
    string reason = "invalid credentials";
  };

  exception InvalidAccountTypeException {
    string reason = "standard users cannot apply for credit";
  };

  exception CurrencyNotSupportedException {
    string reason = "this currency is not supported";
  };

  interface Account {
    AccountType getAccountType();
    Income getAccountIncome();
    CreditData applyForCredit(Currency currency, Income income, RepaymentTime time) throws InvalidAccountTypeException;
  };

  interface AccountStandard extends Account {};
  interface AccountPremium extends Account {};

  interface AccountFactory {
    AccountCreated createAccount(Name name, Surname surname, Pesel pesel, Income income);
    Account* obtainAccess(Pesel pesel) throws InvalidCredentialsException;
  };
  
};
