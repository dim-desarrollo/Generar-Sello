package com.example.demo.Service.Impl;

import com.example.demo.Model.DTO.*;
import com.example.demo.Model.Entity.Sello;
import com.example.demo.Model.Entity.Usuario;
import com.example.demo.Repocitori.SelloRepocitory;
import com.example.demo.Repocitori.UsuarioRepocitory;
import com.example.demo.Service.Interfaz.DatoServicios;
import com.example.demo.Service.Interfaz.SelloServicios;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelloServiceImpl implements SelloServicios {

    private final SelloRepocitory selloRepocitory;
    private final UsuarioRepocitory usuarioRepocitory;

    private final DatoServicios datoServicios;

    private static final Logger logger = LoggerFactory.getLogger(Usuario.class);


    @Override
    public Sello guardar(Sello sello) {
        return selloRepocitory.save(sello);
    }

    @Override
    public Sello actualizar(Long id, Sello entidad) {
        return null;
    }

    @Override
    public Sello buscarPorId(Long id) {
        return null;
    }

    @Override
    public Collection<Sello> buscarTodos() {
        return null;
    }

    @Override
    public Long cantidad() {
        return null;
    }

    @Override
    public void eliminar(Long id) {

    }

    @Override
    public boolean existePorId(Long id) {
        return false;
    }

    @Override
    public ResponceDTO generarSello(DatosDTO datosDTO) {


        try {

            //--TRAE LA CATEGORIA DE EL CONTRIBUYENTE EN BASE A CUIT--

            String cuit = String.valueOf(datosDTO.getCuit());
            //cambiar de vuelta
            Object usu = usuarioRepocitory.findByCuitSqlServer(cuit);

            //si no viene contribuyente en la consulta es por que no esta empadronado
            if (usu == null) {

                ResponceDTO responce = new ResponceDTO("fail"
                        , null
                        , "404"
                        , "com.dim.exception.GenericException.ContribuyenteNoEmpadronado"
                        , "El contribuyente no fue empadronado antes.");
                return responce;

            }

            Object[] usuArray = (Object[]) usu;
            // Crear un nuevo arreglo para almacenar los elementos
            Object[] newArray = new Object[usuArray.length];
            // Copiar los elementos del usuArray al newArray
            for (int i = 0; i < usuArray.length; i++) {
                newArray[i] = usuArray[i];
            }

            System.out.println("cuit: " + newArray[0]);
            System.out.println("categoria: " + newArray[1]);
            System.out.println("anio: " + newArray[2]);

            Usuario contribuyente = Usuario.builder()
                    .cuit(String.valueOf(newArray[0]))
                    .categoria(String.valueOf(newArray[1]))
                    .anio(Integer.parseInt(String.valueOf(newArray[2])))
                    .build();


            if (contribuyente.getAnio() != 2025) { //cambiar ya que este se tiene que poder ser dinamico

                ResponceDTO responce = new ResponceDTO("fail"
                        , null
                        , "404"
                        , "com.dim.exception.GenericException.ContribuyenteNoCategorizadoEnEsteAnio"
                        , "El contribuyente no fue categorizado para el año 2025, ultima categorizacion " + String.valueOf(newArray[2]) + ".");
                return responce;

            }


            //-------------------------------------------------------

            //--TRAMITES---------------------------------------------

            ResponceDTO responce = GetSelloForTramite(datosDTO.getId_tramite(), contribuyente.getCategoria(), contribuyente.getCuit());

            return responce;

            //-------------------------------------------------------


            //Usuario usuario = usuarioRepocitory.findAllByCuit("123456789"); //suplantar con objeto harcodeado



        } catch (Exception e) {

            e.printStackTrace();
            ResponceDTO responce = new ResponceDTO("fail", null, "404", "com.dim.exception.GenericException", "Algo fallo.");
            return responce;

        }

    }

    public ResponceDTO GetSelloForTramite(long id_tramite, String categoria, String cuit) {

        List<Object> respuestaAlmacenada = new ArrayList<>();

        //List<Object[]> resultados = selloRepocitory.obtenerTramite(id_tramite, categoria); //funcion harcodeada

        //obtenerDatos

        int opcion = (int) id_tramite;

        List<Object[]> resultados = selloRepocitory.obtenerSello(id_tramite,categoria,2024);

        //List<Object[]> resultados = obtenerDatos(opcion);

        if (resultados != null) {

            for (Object[] resultado : resultados) {
                for (Object elemento : resultado) {
                    respuestaAlmacenada.add(elemento);
                }

            }

            double costo = 17.0;

            if (id_tramite == 1 || id_tramite == 522 || id_tramite == 549 || id_tramite == 550 || id_tramite == 552 || id_tramite == 561 || id_tramite == 652) {

                System.out.println(respuestaAlmacenada.get(3).toString());

                //Double.parseDouble()

                Double cant_urbanos_inspeccion = Double.parseDouble(respuestaAlmacenada.get(3).toString());
                Double cant_urbanos_tasaAdministrativa = Double.parseDouble(respuestaAlmacenada.get(9).toString());
                Double cant_urbanos_factibilidad = Double.parseDouble(respuestaAlmacenada.get(15).toString());
                Double cant_urbanos_categoria = Double.parseDouble(respuestaAlmacenada.get(21).toString());

                int i = 0;
                for (Object elemento : respuestaAlmacenada) {
                    System.out.println(elemento);
                    i++;
                    System.out.println(i);
                }

                Concepto conceptoDTO = Concepto.builder()
                        .inspeccion(cant_urbanos_inspeccion)
                        .tasaAdministrativa(cant_urbanos_tasaAdministrativa)
                        .factibilidad(cant_urbanos_factibilidad)
                        .categoria(cant_urbanos_categoria)
                        .build();

                SelloDTO selloDTO = SelloDTO.builder()
                        .tipoCategoria(categoria)
                        .importeTotal(conceptoDTO.getCategoria() + conceptoDTO.getFactibilidad() + conceptoDTO.getTasaAdministrativa() + conceptoDTO.getInspeccion())
                        .conceptoDTO(conceptoDTO)
                        .id_tramite(id_tramite)
                        .cuit(cuit)
                        .build();

                ResponceDTO responce = new ResponceDTO("ok", selloDTO, "200 OK", "", "");

                return responce;

            } else if (id_tramite == 2) {

                Double cant_urbanos_reinspeccion = Double.parseDouble(respuestaAlmacenada.get(3).toString());
                String categoria_reinspeccion = categoria;
                //double costo = (double) objetosAlmacenados.get(3);

                ConceptoReinspeccion conceptoReinspeccion = ConceptoReinspeccion.builder()
                        .inspeccion(cant_urbanos_reinspeccion)
                        .build();

                SelloDTO selloDTO = SelloDTO.builder()
                        .tipoCategoria(categoria)
                        .importeTotal(conceptoReinspeccion.getInspeccion())
                        .conceptoDTO(conceptoReinspeccion)
                        .id_tramite(id_tramite)
                        .cuit(cuit)
                        .build();

                ResponceDTO responce = new ResponceDTO("ok", selloDTO, "200 OK", "", "");

                return responce;


            }else{

                ResponceDTO responce = new ResponceDTO("fail", null, "404", "com.dim.exception.GenericException.SelloMalFormet", "Fallo la Creacion del Sello.");
                return responce;

            }


        } else {
            ResponceDTO responce = new ResponceDTO("fail", null, "404", "com.dim.exception.GenericException.SelloMalFormet", "Fallo la Creacion del Sello.");
            return responce;
        }

        //20173762918
        //20121650674

    }



    public ResponceDTO tramite1(long id_tramite,String categoria,String cuit){

        List<Object> objetosAlmacenados = new ArrayList<>();

        List<Object[]> resultados = selloRepocitory.obtenerTramite(id_tramite, categoria);

        if (resultados != null) {

            for (Object[] resultado : resultados) {
                for (Object elemento : resultado) {
                    objetosAlmacenados.add(elemento);
                }
            }

            Long cant_urbanos_inspeccion = Long.parseLong(objetosAlmacenados.get(1).toString());
            Long cant_urbanos_tasaAdministrativa = Long.parseLong(objetosAlmacenados.get(5).toString());
            Long cant_urbanos_factibilidad = Long.parseLong(objetosAlmacenados.get(9).toString());
            Long cant_urbanos_categoria = Long.parseLong(objetosAlmacenados.get(13).toString());
            double costo = (double) objetosAlmacenados.get(3);

            Concepto conceptoDTO = Concepto.builder()
                    .inspeccion(cant_urbanos_inspeccion*costo)
                    .tasaAdministrativa(cant_urbanos_tasaAdministrativa*costo)
                    .factibilidad(cant_urbanos_factibilidad*costo)
                    .categoria(cant_urbanos_categoria*costo)
                    .build();

            SelloDTO selloDTO = SelloDTO.builder()
                    .tipoCategoria(categoria)
                    .importeTotal(conceptoDTO.getCategoria() + conceptoDTO.getFactibilidad() + conceptoDTO.getTasaAdministrativa() + conceptoDTO.getInspeccion())
                    .conceptoDTO(conceptoDTO)
                    .id_tramite(id_tramite)
                    .cuit(cuit)
                    .build();

            ResponceDTO responce = new ResponceDTO("ok",selloDTO,"200 OK","","");

            return responce;

        } else {
            ResponceDTO responce = new ResponceDTO("fail",null,"404","com.dim.exception.GenericException.SelloMalFormet","Fallo la Creacion del Sello.");
            return responce;
        }

    }

    public ResponceDTO tramite2(long id_tramite,String categoria,String cuit){

        List<Object> objetosAlmacenados = new ArrayList<>();

        List<Object[]> resultados = selloRepocitory.obtenerTramite(id_tramite, categoria);

        if (resultados != null) {

            for (Object[] resultado : resultados) {
                for (Object elemento : resultado) {
                    objetosAlmacenados.add(elemento);
                }
            }

            Long cant_urbanos_reinspeccion = Long.parseLong(objetosAlmacenados.get(1).toString());
            String categoria_reinspeccion= objetosAlmacenados.get(6).toString();
            double costo = (double) objetosAlmacenados.get(3);

            ConceptoReinspeccion conceptoReinspeccion = ConceptoReinspeccion.builder()
                    .inspeccion(cant_urbanos_reinspeccion*costo)
                    .build();

            SelloDTO selloDTO = SelloDTO.builder()
                    .tipoCategoria(categoria)
                    .importeTotal(conceptoReinspeccion.getInspeccion())
                    .conceptoDTO(conceptoReinspeccion)
                    .id_tramite(id_tramite)
                    .cuit(cuit)
                    .build();

            ResponceDTO responce = new ResponceDTO("ok",selloDTO,"200 OK","","");

            return responce;

        } else {
            ResponceDTO responce = new ResponceDTO("fail",null,"404","com.dim.exception.GenericException.SelloMalFormet ","Fallo la Creacion del Sello.");
            return responce;
        }

    }


    public List<Object[]> obtenerDatos(int opcion) {
        List<Object[]> datos = new ArrayList<>();

        if (opcion == 1) {
            datos.add(new Object[]{1, 1, "Inspeccion", 550,17});
            datos.add(new Object[]{1, 2, "Tasa Administrativa", 20,17});
            datos.add(new Object[]{1, 3, "Factibilidad", 150,17});
            datos.add(new Object[]{1, 4, "Categoria C", 150,17});
        } else if (opcion == 2) {
            datos.add(new Object[]{2, 5, "Re-Inspeccion", 150,17});
        } else {
            throw new IllegalArgumentException("Opción no válida");
        }

        return datos;
    }

}
                /*
            Object[] resultado = selloRepocitory.obtenerTramite(datosDTO.getId_tramite()).stream().findFirst().orElse(null);


                for (Object elemento : resultado) {

                    // Realizar alguna acción con cada elemento (por ejemplo, imprimirlo)
                    System.out.println(elemento);
                }

            System.out.println(objetosAlmacenados.get(12).toString());
            System.out.println(objetosAlmacenados.get(13).toString());
            System.out.println(objetosAlmacenados.get(14).toString());
            System.out.println(objetosAlmacenados.get(15).toString());





            if (datosDTO.getId_tramite() == 1) {

                if (usuario == null) {

                    ResponceDTO responce = new ResponceDTO("fail"
                            , null
                            , "404"
                            , "com.dim.exception.GenericException.ContribuyenteNoEmpadronado"
                            , "El contribuyente no fue empadronado antes.");
                    return responce;

                }

                if (usuario.getAnio() != 2024) {

                    System.out.println(usuario.getAnio());
                    ResponceDTO responce = new ResponceDTO("fail"
                            , null
                            , "404"
                            , "com.dim.exception.GenericException.ContribuyenteNoCategorizadoEnEsteAnio"
                            , "El contribuyente no fue categorizado para el año 2024, ultima categorizacion " + usuario.getAnio() + ".");
                    return responce;

                }

                if (usuario != null) {

                    ResponceDTO responce = tramite1(datosDTO.getId_tramite(), usuario.getCategoria(), usuario.getCuit());
                    return responce;

                } else {

                    ResponceDTO responce = new ResponceDTO("fail", null, "404", "GenericException", "El contribuyente no fue empadronado antes.");
                    return responce;

                }

            } else if (datosDTO.getId_tramite() == 2) {

                //Usuario usuario = usuarioRepocitory.findAllByCuit(Long.toString(datosDTO.getCuit()));

                Usuario usuario2 = usuarioRepocitory.findAllByCuit("123456789"); //suplantar con objeto harcodeadoU


                if (usuario2 == null) {

                    ResponceDTO responce = new ResponceDTO("fail"
                            , null
                            , "404"
                            , "com.dim.exception.GenericException.ContribuyenteNoEmpadronado"
                            , "El contribuyente no fue empadronado antes.");
                    return responce;

                }

                if (usuario2.getAnio() != 2024) {

                    System.out.println(usuario.getAnio());
                    ResponceDTO responce = new ResponceDTO("fail"
                            , null
                            , "404"
                            , "com.dim.exception.GenericException.ContribuyenteNoCategorizadoEnEsteAnio"
                            , "El contribuyente no fue categorizado para el año 2024, ultima categorizacion " + usuario.getAnio() + ".");
                    return responce;

                }

                if (usuario != null) {

                    ResponceDTO responce = tramite2(datosDTO.getId_tramite(), usuario.getCategoria(), usuario.getCuit());
                    return responce;

                } else {

                    ResponceDTO responce = new ResponceDTO("fail", null, "404", "GenericException", "El contribuyente no fue empadronado antes.");
                    return responce;

                }

            } else {

                ResponceDTO responce = new ResponceDTO("fail", null, "404", "GenericException", "id_tramite no encontrado");
                return responce;

            }

                */