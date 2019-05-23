import Ice
import sys
import os
import signal
import grpc
import random
from threading import Thread
import threading

sys.path.append(os.path.abspath("./utils/out/proto"))

import exchange_pb2
import exchange_pb2_grpc

sys.path.append(os.path.abspath("./utils/out/ice"))

from BankSystem import *
from currency_rates import currency_rates


class InvalidAccountTypeExceptionI(InvalidAccountTypeException):
    pass


class InvalidCredentialsExceptionI(InvalidCredentialsException):
    pass


class CurrencyNotSupportedExceptionI(CurrencyNotSupportedException):
    pass


class AccountI(Account):
    def __init__(self, accountType, name, surname, pesel, password, income):
        self.accountType = accountType
        self.name = name
        self.surname = surname
        self.pesel = pesel
        self.password = password
        self.income = Income(income.value)

    def getAccountIncome(self, current):
        print('{} checked income'.format(self.pesel))
        return self.income


class AccountStandardI(AccountI, AccountStandard):
    def applyForCredit(self, currency, amount, time, current):
        raise InvalidAccountTypeExceptionI


class AccountPremiumI(AccountI, AccountPremium):
    def applyForCredit(self, currency, amount, time, current):
        if currency.value not in currencies:
            raise CurrencyNotSupportedExceptionI
        credit_value = currency_rates[currency.value] * amount.value
        print('{} applied for credit'.format(self.pesel))
        return CreditData(amount, Income(credit_value))


class AccountFactoryI(AccountFactory):
    def __init__(self):
        self.accountMap = {}

    def createAccount(self, name, surname, pesel, income, current):
        password = Password(str(random.randint(100, 999)))
        if income.value > 100000:
            acc_type = AccountType.PREMIUM
            account = AccountPremiumI(acc_type, name, surname, pesel, password, income)
        else:
            acc_type = AccountType.STANDARD
            account = AccountStandardI(acc_type, name, surname, pesel, password, income)

        asm_id = str(pesel.value) + '_' + acc_type.name
        self.accountMap[str(pesel.value) + password.value] = asm_id

        current.adapter.add(account, Ice.stringToIdentity(asm_id))
        print('created account: {} {}'.format(pesel.value, acc_type.name))
        return AccountCreated(password, account.accountType)

    def obtainAccess(self, pesel, current):
        try:
            asm_id = self.accountMap[str(pesel.value) + current.ctx['password']]
            acc_prx = AccountPrx.checkedCast(current.adapter.createProxy(Ice.stringToIdentity(asm_id)))
        except Exception:
            raise InvalidCredentialsExceptionI
        else:
            print('{} {} accessed account'.format(pesel.value, current.ctx))
            return acc_prx


def connect_to_exchange(currency_rates):
    channel = grpc.insecure_channel('localhost:50051')
    stub = exchange_pb2_grpc.ExchangeStub(channel)
    request = exchange_pb2.ExchangeRequest(origin_currency=exchange_pb2.PLN, currency_rates=currency_rates)
    try:
        for response in stub.subscribeExchangeRate(request):
            print(currency_rates)
            currency_rates[response.currency-1] = response.ExchangeRate
    except Exception as e:
        print(e)


def exit_bank(signum, frame):
    for thread in threading.enumerate():
        if thread.is_alive():
            thread._stop()
    communicator.shutdown()


with Ice.initialize(sys.argv, sys.argv[1]) as communicator:
    if __name__ == "__main__":
        currencies = list(map(lambda e: int(e), sys.argv[2:]))
        exchange_thread = Thread(target=connect_to_exchange, args=(currencies,))
        exchange_thread.start()

    signal.signal(signal.SIGINT, exit_bank)
    adapter = communicator.createObjectAdapter("AccountFactory")
    adapter.add(AccountFactoryI(), Ice.stringToIdentity("accountFactory"))
    adapter.activate()
    communicator.waitForShutdown()
