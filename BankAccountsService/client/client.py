import Ice
import sys
import os

sys.path.append(os.path.abspath("./utils/out/ice"))

from BankSystem import *


def get_currency_code(currency):
    currencies = {'PLN': Currency.PLN, 'EUR': Currency.EUR, 'USD': Currency.USD, 'GBP': Currency.GBP,
                  'CHF': Currency.CHF}
    return currencies.get(currency.upper(), 'no such currency available')


def run(communicator):
    server = AccountFactoryPrx.checkedCast(
        communicator.propertyToProxy('AccountFactory.Proxy').ice_twoway().ice_secure(False))

    account = None
    print('Welcome to the bank service\n'
          'Available commands:\n'
          'signup\n'
          'income\n'
          'credit (only for premium clients)\n')
    while True:
        command = input()
        if command == 'signup':
            name = input('name: ')
            surname = input('surname: ')
            pesel = input('pesel: ')
            income = input('declared monthly income: ')
            try:
                created_account = server.createAccount(Name(name), Surname(surname), Pesel(int(pesel)),
                                                       Income(int(income)))
            except Exception as error:
                print(error)
            else:
                print(created_account)
        else:
            pesel = input('pesel: ')
            password = input('password: ')
            password_dict = {'password': password}
            try:
                account = server.obtainAccess(Pesel(int(pesel)), password_dict)
            except Exception as error:
                print(error)
            else:
                print(account)
            if command == 'income':
                print('your income: {}'.format(account.getAccountIncome()))
            elif command == 'credit':
                currency = input('currency: ')
                amount = input('amount: ')
                time = input('repayment time (years): ')
                try:
                    credit_data = account.applyForCredit(get_currency_code(currency),
                                                         Income(int(amount)),
                                                         RepaymentTime(time))
                except Exception as error:
                    print(error)
                else:
                    print(credit_data)


if __name__ == "__main__":
    with Ice.initialize(sys.argv, "./client/config.client") as communicator:
        run(communicator)
