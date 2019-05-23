import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import protos.ExchangeGrpc;
import protos.ExchangeProto.*;
import protos.ExchangeProto.Currency;

import java.io.IOException;
import java.util.*;

public class Exchange {

    private Server server;
    private static final int PORT = 50051;

    public static void main(String[] args) throws IOException, InterruptedException {
        final Exchange exchange = new Exchange();
        exchange.start();
        exchange.blockUntilShutdown();
    }

    private void start() throws IOException {
        Service service = new Service();
        service.initializeExchange();

        server = ServerBuilder.forPort(PORT)
                .addService(service)
                .build()
                .start();

        System.out.println("Server is running at: " + server.getListenSockets());
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null)
            server.awaitTermination();
    }

    private static class Service extends ExchangeGrpc.ExchangeImplBase {

        private static final Double CURRENCY_INIT_VALUE = 1.0;
        private final Map<Currency, Double> exchange = new LinkedHashMap<>();
        private final Map<Currency, List<StreamObserver<ExchangeStream>>> subscribers = new HashMap<>();

        private synchronized Map<Currency, List<StreamObserver<ExchangeStream>>> getSubscribersSync() {
            return subscribers;
        }

        private void initializeExchange() {
            for (Currency currency : Currency.values()) {
                exchange.put(currency, CURRENCY_INIT_VALUE);
                getSubscribersSync().computeIfAbsent(currency, k -> new ArrayList<>());
            }
            new Thread(exchangeRunnable).start();
        }

        private final Runnable exchangeRunnable = () -> {
            while (true) {
                Random random = new Random();
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int currencyIndex = random.nextInt(Currency.values().length);
                Currency currency = exchange.keySet().toArray(new Currency[0])[currencyIndex];
                Double currencyRate = random.nextDouble() * 10;
                exchange.put(currency, currencyRate);

                List<StreamObserver<ExchangeStream>> currencySubscribers = getSubscribersSync().get(currency);
                List<StreamObserver<ExchangeStream>> subscribersToDelete = new ArrayList<>();

                for (StreamObserver<ExchangeStream> observer : currencySubscribers) {
                    ExchangeStream rate = ExchangeStream.newBuilder()
                            .setCurrency(currency)
                            .setExchangeRate(currencyRate)
                            .build();
                    try {
                        observer.onNext(rate);
                    } catch (StatusRuntimeException e) {
                        System.out.println(observer.toString() + " not responding, removing from subscribers of " + currency.name());
                        subscribersToDelete.add(observer);
                    }

                }
                currencySubscribers.removeAll(subscribersToDelete);
            }
        };

        @Override
        public void subscribeExchangeRate(ExchangeRequest request, StreamObserver<ExchangeStream> responseObserver) {
            Currency originCurrency = request.getOriginCurrency();
            List<Currency> ratesList = request.getCurrencyRatesList();
            ratesList.remove(originCurrency);
            System.out.println(responseObserver + " as new subscriber requested: " + ratesList);
            for (Currency currency : ratesList) {
                getSubscribersSync().get(currency).add(responseObserver);
            }
        }
    }
}
