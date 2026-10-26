package com.example.book.service;

import com.example.book.messaging.LoanEvent;

public interface StockService {

    void apply(LoanEvent event);
}
