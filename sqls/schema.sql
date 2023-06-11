CREATE TABLE rate_limit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    route_id VARCHAR(255) NOT NULL,
    limit_for_minutes INT NOT NULL,
    request_count INT NOT NULL,
    last_request_timestamp TIMESTAMP NOT NULL
);
