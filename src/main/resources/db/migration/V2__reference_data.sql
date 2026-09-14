INSERT INTO department(name) VALUES ('Engineering'),('Product'),('Sales'),('Marketing'),('Finance'),('People'),('Operations'),('Customer Success');
INSERT INTO job_level(code,name,rank_order) VALUES ('L1','Associate',1),('L2','Professional',2),('L3','Senior',3),('L4','Lead',4),('L5','Director',5);
INSERT INTO country(code,name,currency_code) VALUES ('US','United States','USD'),('IN','India','INR'),('GB','United Kingdom','GBP'),('DE','Germany','EUR'),('CA','Canada','CAD'),('SG','Singapore','SGD');
INSERT INTO fx_rate_set(as_of_date,reporting_currency) VALUES ('2026-01-01','USD');
INSERT INTO fx_rate(rate_set_id,currency_code,units_per_reporting_currency)
SELECT id,currency,rate FROM fx_rate_set CROSS JOIN (VALUES ('USD',1.000000),('INR',85.500000),('GBP',0.790000),('EUR',0.920000),('CAD',1.380000),('SGD',1.350000)) v(currency,rate)
WHERE reporting_currency='USD';

