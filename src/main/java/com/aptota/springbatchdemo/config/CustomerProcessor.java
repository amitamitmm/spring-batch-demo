package com.aptota.springbatchdemo.config;

import com.aptota.springbatchdemo.model.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {
    @Override
    public Customer process(Customer item) throws Exception {
     /*   if(item.getId()<200){
            return item;
        }*/
        return item;
    }
}
