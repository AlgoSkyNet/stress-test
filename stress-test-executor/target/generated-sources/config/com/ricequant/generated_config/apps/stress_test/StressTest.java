//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.20 at 01:31:36 AM HKT 
//


package com.ricequant.generated_config.apps.stress_test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;sequence>
 *           &lt;element ref="{http://www.ricequant.com/generated-config/apps/stress-test}server"/>
 *           &lt;element ref="{http://www.ricequant.com/generated-config/apps/stress-test}scenarios"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "server",
    "scenarios"
})
@XmlRootElement(name = "stressTest")
public class StressTest {

    @XmlElement(required = true)
    protected ServerType server;
    @XmlElement(required = true)
    protected ScenariosType scenarios;

    /**
     * Gets the value of the server property.
     * 
     * @return
     *     possible object is
     *     {@link ServerType }
     *     
     */
    public ServerType getServer() {
        return server;
    }

    /**
     * Sets the value of the server property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServerType }
     *     
     */
    public void setServer(ServerType value) {
        this.server = value;
    }

    /**
     * Gets the value of the scenarios property.
     * 
     * @return
     *     possible object is
     *     {@link ScenariosType }
     *     
     */
    public ScenariosType getScenarios() {
        return scenarios;
    }

    /**
     * Sets the value of the scenarios property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScenariosType }
     *     
     */
    public void setScenarios(ScenariosType value) {
        this.scenarios = value;
    }

}
