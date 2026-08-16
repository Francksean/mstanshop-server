-- Cash on delivery support: payments now carry a method discriminator, and CamPay-specific
-- reference columns become optional since a COD payment never calls CamPay.
ALTER TABLE payments ADD COLUMN method VARCHAR(255) NOT NULL DEFAULT 'MOBILE_PAYMENT';
ALTER TABLE payments ALTER COLUMN external_reference DROP NOT NULL;
