create database mew;
use mew;
drop TABLE IF EXISTS nodes;
create table mew.nodes (DeviceID varchar(30), battery double, linkspeed int(11), AccRunning boolean, AccPower double, GPSRunning boolean, GPSPower double, GyrRunning boolean, GyrPower double, SensorsAvailable int, servicingTask boolean, old_lat double, old_lon double, new_lat double, new_lon double, providerMode boolean, MicPower double,
WiFiPower double, PRIMARY KEY (DeviceID));

-- NODES AUDIT TABLE
drop TABLE IF EXISTS nodes_audit;
create table mew.nodes_audit (DeviceID varchar(30), battery double, linkspeed int(11), AccRunning boolean, AccPower double, 
					GPSRunning boolean, GPSPower double, GyrRunning boolean, GyrPower double, SensorsAvailable int, servicingTask boolean, 
                    old_lat double, old_lon double, new_lat double, new_lon double, providerMode boolean, MicPower double, WiFiPower double,Actions VARCHAR(8),
                    dt_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, unix_timestamp bigint(16), 
                    PRIMARY KEY (DeviceID, unix_timestamp));

-- NODES TABLE TRIGGERS
DROP TRIGGER IF EXISTS mew.nodes__ai;
DROP TRIGGER IF EXISTS mew.nodes__au;
DROP TRIGGER IF EXISTS mew.nodes__ad;

CREATE TRIGGER mew.nodes__ai AFTER INSERT ON mew.nodes FOR EACH ROW
    INSERT INTO mew.nodes_audit SELECT d.*, 'insert', NOW() , ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)
    FROM mew.nodes AS d WHERE d.DeviceID = NEW.DeviceID;

CREATE TRIGGER mew.nodes__au AFTER UPDATE ON mew.nodes FOR EACH ROW
    INSERT INTO mew.nodes_audit SELECT  d.*, 'update', NOW(), ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)
    FROM mew.nodes AS d WHERE d.DeviceID = NEW.DeviceID;

CREATE TRIGGER mew.nodes__ad BEFORE DELETE ON mew.nodes FOR EACH ROW
    INSERT INTO mew.nodes_audit SELECT  d.*, 'delete', NOW() , ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)
    FROM mew.nodes AS d WHERE d.DeviceID = OLD.DeviceID;


-- NVALUES TABLE
drop TABLE IF EXISTS mew.nvalues;

create table mew.nvalues (DeviceID varchar(30), nvalue double, weight double, existing boolean,
PRIMARY KEY (DeviceID) );

-- NVALUES AUDIT
drop TABLE IF EXISTS mew.nvalues_audit;

create table mew.nvalues_audit (DeviceID varchar(30), nvalue double, weight double, existing boolean,
	Actions VARCHAR(8), dt_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, unix_timestamp bigint(16),
	PRIMARY KEY (DeviceID, unix_timestamp));


-- NVALUES TRIGGERS
DROP TRIGGER IF EXISTS mew.nvalues__ai;
DROP TRIGGER IF EXISTS mew.nvalues__au;
DROP TRIGGER IF EXISTS mew.nvalues__bd;

CREATE TRIGGER mew.nvalues__ai AFTER INSERT ON mew.nvalues FOR EACH ROW
    INSERT INTO mew.nvalues_audit SELECT d.*, 'insert', NOW() , ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)
    FROM mew.nvalues AS d WHERE d.DeviceID = NEW.DeviceID;

CREATE TRIGGER mew.nvalues__au AFTER UPDATE ON mew.nvalues FOR EACH ROW
    INSERT INTO mew.nvalues_audit SELECT  d.*, 'update', NOW(), ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000) 
    FROM mew.nvalues AS d WHERE d.DeviceID = NEW.DeviceID;

CREATE TRIGGER mew.nvalues__bd BEFORE DELETE ON mew.nvalues FOR EACH ROW
    INSERT INTO mew.nvalues_audit SELECT  d.*, 'delete', NOW() , ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)
    FROM mew.nvalues AS d WHERE d.DeviceID = OLD.DeviceID;

	
-- QUERIES
drop table IF EXISTS mew.queries;
create table mew.queries (_id int not null auto_increment , QueryID varchar(30),ProviderID varchar(30),
									QueryAllocation varchar(255), serviced int default 0, primary key(_id));


-- QUERIES AUDIT
drop table if EXISTS mew.queries_audit;
create table mew.queries_audit (_id int, QueryID varchar(30),ProviderID varchar(30),QueryAllocation varchar(300),
				serviced int default 0, Actions VARCHAR(8), dt_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
				unix_timestamp bigint(16), primary key(_id, Actions, unix_timestamp));

-- QUERIES TRIGGERS
DROP TRIGGER IF EXISTS mew.queries__ai;
DROP TRIGGER IF EXISTS mew.queries__au;
DROP TRIGGER IF EXISTS mew.queries__bd;

CREATE TRIGGER mew.queries__ai AFTER INSERT ON mew.queries FOR EACH ROW
    INSERT INTO mew.queries_audit SELECT d.*, 'insert', NOW() , ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)
    FROM mew.queries AS d WHERE d._id = NEW._id;

CREATE TRIGGER mew.queries__au AFTER UPDATE ON mew.queries FOR EACH ROW
    INSERT INTO mew.queries_audit SELECT  d.*, 'update', NOW(), ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000) 
    FROM mew.queries AS d WHERE d._id = NEW._id;

CREATE TRIGGER mew.queries__bd BEFORE DELETE ON mew.queries FOR EACH ROW
    INSERT INTO mew.queries_audit SELECT  d.*, 'delete', NOW() , ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)
    FROM mew.queries AS d WHERE d._id = OLD._id;
