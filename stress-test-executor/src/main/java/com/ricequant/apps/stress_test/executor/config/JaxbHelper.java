package com.ricequant.apps.stress_test.executor.config;

import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenfeng
 */
public class JaxbHelper<T> {

  private SchemaFactory iSchemaFacotry;

  private Unmarshaller iUnmarshaller;

  public JaxbHelper(Class<T> clazz) {
    iSchemaFacotry = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
      iUnmarshaller = jaxbContext.createUnmarshaller();
    }
    catch (JAXBException e) {
      throw new RuntimeException("Cannot create jaxb unmarshaller.", e);
    }
  }

  @SuppressWarnings("unchecked")
  public T loadXml(boolean useNonXmlRoot, String xmlFileName, String... sourceNames) {
    List<Source> sources = loadSchemaSources(sourceNames);


    Schema schema;
    try {
      schema = iSchemaFacotry.newSchema(sources.toArray(new Source[sources.size()]));
    }
    catch (SAXException e) {
      throw new IllegalArgumentException("Unable to resolve schema files: " + xmlFileName, e);
    }

    Validator validator = schema.newValidator();
    Object result;
    try {
      validator.validate(loadStreamSource(xmlFileName));
      result = iUnmarshaller.unmarshal(loadSchemaSource(xmlFileName));
    }
    catch (UnmarshalException ue) {
      throw new RuntimeException("Unmarshall exception: " + ue.getMessage(), ue);
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to parse xml file", e);
    }
    if (useNonXmlRoot) {
      return ((JAXBElement<T>) result).getValue();
    }
    return (T) result;
  }

  private StreamSource loadStreamSource(String fileName) {
    StreamSource streamSource;
    File file = new File(fileName);
    if (!file.exists()) {
      System.err.println("Loading file from classpath");
      InputStream xmlFileStream = loadStreamFromClassPath(fileName);
      if (xmlFileStream != null) {
        streamSource = new StreamSource(xmlFileStream);
      }
      else
        throw new IllegalArgumentException("File not found: " + fileName);
    }
    else {
      streamSource = new StreamSource(file);
    }

    return streamSource;
  }

  private List<Source> loadSchemaSources(String... sourceNames) {
    List<Source> ret = new ArrayList<>();
    for (String sourceName : sourceNames)
      ret.add(loadSchemaSource(sourceName));

    return ret;
  }

  private Source loadSchemaSource(String sourceName) {
    File sourceFile = new File(sourceName);

    if (sourceFile.exists())
      return new StreamSource(sourceFile.getAbsolutePath());

    InputStream inputSource = loadStreamFromClassPath(sourceName);
    if (inputSource != null)
      return new StreamSource(inputSource);

    throw new IllegalArgumentException("Schema file not found: " + sourceName);
  }

  private InputStream loadStreamFromClassPath(String resource) {
    return getClass().getClassLoader().getResourceAsStream(resource);
  }
}
