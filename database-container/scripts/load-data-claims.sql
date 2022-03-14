delete
from claim_drug;
delete
from claim_inpatient;
delete
from claim_outpatient;
delete
from drug;
delete
from facility;
delete
from procedure;
delete
from claim;
delete
from provider;
delete
from eligibility;
delete
from plan;
delete
from member;
delete
from diagnosis;
delete
from address;

insert into address (street_1, street_2, city, state, zip)
values ('3 Bowdoin St', null, 'Boston', 'MA', '02110');
insert into address (street_1, street_2, city, state, zip)
values ('5 Milk St', null, 'Boston', 'MA', '02110');
insert into address (street_1, street_2, city, state, zip)
values ('9 Harrison St', null, 'Boston', 'MA', '02110');
insert into address (street_1, street_2, city, state, zip)
values ('32 Berkley Road', 'Suite 2', 'Boston', 'MA', '02110');
insert into address (street_1, street_2, city, state, zip)
values ('6 Peterson Blvd', 'Suite 9', 'Boston', 'MA', '02110');
insert into address (street_1, street_2, city, state, zip)
values ('10 Longwood Ave', null, 'Boston', 'MA', '02110');
insert into address (street_1, street_2, city, state, zip)
values ('50 Brookline Road', null, 'Boston', 'MA', '02110');
insert into address (street_1, street_2, city, state, zip)
values ('66 Brookline Ave', 'Apt 220', 'Medford', 'MA', '02312');

insert into diagnosis (code, description)
values ('F23', 'Brief psychotic disorder');
insert into diagnosis (code, description)
values ('A00', 'Cholera');
insert into diagnosis (code, description)
values ('A03', 'Shigellosis');
insert into diagnosis (code, description)
values ('A07', 'Other protozoal intestinal diseases');
insert into diagnosis (code, description)
values ('B01', 'Varicella [chickenpox]');
insert into diagnosis (code, description)
values ('B05', 'Measles');
insert into diagnosis (code, description)
values ('J02', 'Acute pharyngitis');

insert into plan (name, description)
values ('Medicaid', 'State sponsored plan backed by Federal resources');
insert into plan (name, description)
values ('Medicare', 'Federal government-sponsored plan');
insert into plan (name, description)
values ('Tufts Health Plan -PPO', 'Private insurance');
insert into plan (name, description)
values ('BCBS - HMO', 'Private insurance');

insert into member (subscriber_number, first_name, last_name, gender, dob, address_id)
values ('MED9292292-102', 'Peter', 'Scott', 'Male', '06/07/1981',
        (select id from address where street_1 = '3 Bowdoin St'));
insert into member (subscriber_number, first_name, last_name, gender, dob, address_id)
values ('MED9292292-882', 'Sam', 'Lewis', 'Male', '08/12/1966',
        (select id from address where street_1 = '5 Milk St'));
insert into member (subscriber_number, first_name, last_name, gender, dob, address_id)
values ('MED9292292-884', 'Marina', 'Escobar', 'Female', '09/03/1977',
        (select id from address where street_1 = '9 Harrison St'));
insert into member (subscriber_number, first_name, last_name, gender, dob, address_id)
values ('MED9292292-885', 'Patricia', 'Walden', 'Female', '12/02/1976',
        (select id from address where street_1 = '50 Brookline Road'));
insert into member (subscriber_number, first_name, last_name, gender, dob, address_id)
values ('MED9292292-889', 'Joseph', 'Arnold', 'Male', '03/02/1966',
        (select id from address where street_1 = '66 Brookline Ave'));

insert into eligibility (plan_id, member_id, effective_date, end_date)
values ((select id from plan where name = 'Medicaid'),
        (select id from member where subscriber_number = 'MED9292292-102'), '01/01/2010', '12/31/2025');
insert into eligibility (plan_id, member_id, effective_date, end_date)
values ((select id from plan where name = 'Medicare'),
        (select id from member where subscriber_number = 'MED9292292-882'), '01/01/2009', '12/31/2020');
insert into eligibility (plan_id, member_id, effective_date, end_date)
values ((select id from plan where name = 'Tufts Health Plan -PPO'),
        (select id from member where subscriber_number = 'MED9292292-884'), '01/01/2009', '09/30/2021');
insert into eligibility (plan_id, member_id, effective_date, end_date)
values ((select id from plan where name = 'Medicare'),
        (select id from member where subscriber_number = 'MED9292292-885'), '01/01/2009', '09/30/2021');
insert into eligibility (plan_id, member_id, effective_date, end_date)
values ((select id from plan where name = 'Medicaid'),
        (select id from member where subscriber_number = 'MED9292292-889'), '01/01/2009', '09/30/2021');

insert into provider (npi, first_name, last_name, gender, address_id)
values ('123-90003030', 'David', 'Peterson', 'Male', (select id from address where street_1 = '6 Peterson Blvd'));
insert into provider (npi, first_name, last_name, gender, address_id)
values ('108-12312331', 'Lucas', 'Smith', 'Male', (select id from address where street_1 = '32 Berkley Road'));

insert into drug (ndc, name, description)
values ('0000-000', 'Load Test Drug',
        'This is a made up drug to be used for load testing only');
insert into drug (ndc, name, description)
values ('16590-323', 'Abilify',
        'Abilify is a prescription medicine used to treat the symptoms of schizophrenia, bipolar I disorder (manic depression), and major depressive disorder. Abilify may be used alone or with other medications. Abilify is an antipsychotic, antimanic agent.');
insert into drug (ndc, name, description)
values ('0074-3727', 'Synthroid',
        'Levothyroxine is used to treat an underactive thyroid (hypothyroidism). It replaces or provides more thyroid hormone, which is normally produced by the thyroid gland.');
insert into drug (ndc, name, description)
values ('63479-1802', 'R02 Bile Ducts',
        'R02 Bile Ducts with NDC 63479-1802 is a a human over the counter drug product labeled by Apex Energetics Inc.');
insert into drug (ndc, name, description)
values ('0054-0062', 'Citalopram',
        'Citalopram with NDC 0054-0062 is a a human prescription drug product labeled by West-ward Pharmaceuticals Corp');

insert into procedure (code, description)
values ('D0000ZZ', 'Section D - Radiation Therapy');
insert into procedure (code, description)
values ('8C01X6J', 'Collection of Cerebrospinal Fluid from Indwelling Device in Nervous System');
insert into procedure (code, description)
values ('GZJZZZZ', 'Light Therapy');
insert into procedure (code, description)
values ('7W00X0Z', 'Osteopathic Treatment of Head using Articulatory-Raising Forces');

insert into facility (name, address_id)
values ('St Francis Hospital', (select id from address where street_1 = '10 Longwood Ave'));
insert into facility (name, address_id)
values ('Cathedral United', (select id from address where street_1 = '50 Brookline Road'));