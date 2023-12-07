ALTER TABLE READ_PROGRESS
    ADD COLUMN device_id varchar default '';
ALTER TABLE READ_PROGRESS
    ADD COLUMN device_name varchar default '';
ALTER TABLE READ_PROGRESS
    ADD COLUMN locator blob null;
