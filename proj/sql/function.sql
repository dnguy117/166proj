DROP SEQUENCE IF EXISTS seq_customer_id;
DROP SEQUENCE IF EXISTS seq_mechanic_id;
DROP SEQUENCE IF EXISTS seq_ownership_id;
DROP SEQUENCE IF EXISTS seq_rid_id;
DROP SEQUENCE IF EXISTS seq_wid_id;
--DROP TRIGGER IF EXISTS trg_customer_id;

---------------
-- SEQUENCES --
---------------

CREATE SEQUENCE seq_customer_id START WITH 500 INCREMENT BY 1;
CREATE SEQUENCE seq_mechanic_id START WITH 250 INCREMENT BY 1;
CREATE SEQUENCE seq_ownership_id START WITH 5000 INCREMENT BY 1;
CREATE SEQUENCE seq_rid_id START WITH 30001 INCREMENT BY 1;
CREATE SEQUENCE seq_wid_id START WITH 30001 INCREMENT BY 1;

-----------------
-- PROCUEDURES --
-----------------

--CREATE LANGUAGE plpgsql;
--CREATE OR REPLACE FUNCTION next_customer_id()
--	RETURNS "trigger" AS
--	$BODY$
--	BEGIN
--	NEW := nextval('seq_customer_id');
--	RETURN NEW;
--	END;
--	$BODY$
--	LANGUAGE plpgsql VOLATILE;


--------------
-- TRIGGERS --
--------------

--CREATE TRIGGER trg_customer_id BEFORE INSERT 
--ON Customer EXECUTE PROCEDURE next_customer_id();

