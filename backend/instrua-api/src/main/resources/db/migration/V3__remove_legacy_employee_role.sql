update employees
set access_role = 'COMPANY_ADMIN'
where access_role = 'EMPLOYEE';

alter table employees
    alter column access_role set default 'CLINICAL'
