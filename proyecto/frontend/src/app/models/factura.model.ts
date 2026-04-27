export interface Factura {
  id: number;
  cus: string;
  fechaPago: string;
  responsabilidadFiscal: string;
  tipoDocumento: string;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  telefono: string;
  municipio: string;
  direccion: string;
  email: string;
  valorPack: number;
  valorPackIva: number;
  comentarios: string;
  estado: string;
  fechaCreacion: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface EstadoResumen {
  [key: string]: number;
}
