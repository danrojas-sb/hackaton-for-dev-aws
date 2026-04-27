import mysql from 'mysql2/promise';
import { SQSClient, SendMessageCommand } from '@aws-sdk/client-sqs';

const {
  DB_HOST,
  DB_PORT,
  DB_USER,
  DB_PASSWORD,
  DB_NAME,
  SQS_RESPONSE_URL,
} = process.env;

const sqsClient = new SQSClient({
  region: 'us-east-1',
  endpoint: 'http://localhost:4566',
});

/**
 * Creates a MySQL connection to db_procesos_masivos.
 * @returns {Promise<mysql.Connection>} database connection
 */
const createDbConnection = async () => {
  return mysql.createConnection({
    host: DB_HOST,
    port: parseInt(DB_PORT, 10) || 3306,
    user: DB_USER,
    password: DB_PASSWORD,
    database: DB_NAME,
  });
};

/**
 * Sends a response message to the SQS response queue.
 * @param {object} messageBody - the response payload
 */
const sendResponseMessage = async (messageBody) => {
  const command = new SendMessageCommand({
    QueueUrl: SQS_RESPONSE_URL,
    MessageBody: JSON.stringify(messageBody),
  });
  await sqsClient.send(command);
};

/**
 * Inserts an invoice record into facturas_contabilizadas.
 * @param {mysql.Connection} connection - active DB connection
 * @param {object} factura - parsed invoice data from SQS message
 */
const insertFacturaContabilizada = async (connection, factura) => {
  const sql = `
    INSERT INTO facturas_contabilizadas (
      cus, fecha_pago, responsabilidad_fiscal, tipo_documento,
      numero_documento, nombres, apellidos, telefono,
      municipio, direccion, email, valor_pack,
      valor_pack_iva, comentarios, estado_contable, factura_origen_id
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONTABILIZADA', ?)
  `;

  const params = [
    factura.cus,
    factura.fechaPago,
    factura.responsabilidadFiscal,
    factura.tipoDocumento,
    factura.numeroDocumento,
    factura.nombres,
    factura.apellidos,
    factura.telefono,
    factura.municipio,
    factura.direccion,
    factura.email,
    factura.valorPack,
    factura.valorPackIva,
    factura.comentarios,
    factura.facturaId,
  ];

  await connection.execute(sql, params);
};

/**
 * Lambda handler that processes SQS events.
 * Parses each record, inserts into facturas_contabilizadas,
 * and sends a success/error response to the response queue.
 * @param {object} event - SQS event with Records array
 */
export const handler = async (event) => {
  for (const record of event.Records) {
    let factura;
    let connection;

    try {
      factura = JSON.parse(record.body);
    } catch (parseError) {
      console.error('Error parsing SQS message body:', parseError.message);
      continue;
    }

    try {
      connection = await createDbConnection();
      await insertFacturaContabilizada(connection, factura);

      await sendResponseMessage({
        facturaId: factura.facturaId,
        status: 'TERMINADO',
      });
    } catch (error) {
      console.error(`Error processing factura ${factura.facturaId}:`, error.message);

      await sendResponseMessage({
        facturaId: factura.facturaId,
        status: 'ERROR',
        errorMessage: error.message,
      });
    } finally {
      if (connection) {
        await connection.end();
      }
    }
  }
};
