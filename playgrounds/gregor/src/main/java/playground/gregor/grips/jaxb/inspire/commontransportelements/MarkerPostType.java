//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2011.08.04 at 02:05:46 PM CEST
//


package playground.gregor.grips.jaxb.inspire.commontransportelements;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import net.opengis.gml.v_3_2_1.LengthType;
import net.opengis.gml.v_3_2_1.ReferenceType;


/**
 * <p>Java class for MarkerPostType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MarkerPostType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}TransportPointType">
 *       &lt;sequence>
 *         &lt;element name="location" type="{http://www.opengis.net/gml/3.2}LengthType"/>
 *         &lt;element name="route" type="{http://www.opengis.net/gml/3.2}ReferenceType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkerPostType", propOrder = {
		"rest"
})
public class MarkerPostType
extends TransportPointType
{

	@XmlElementRefs({
		@XmlElementRef(name = "route", namespace = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "location", namespace = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0", type = JAXBElement.class)
	})
	protected List<JAXBElement<?>> rest;

	/**
	 * Gets the rest of the content model.
	 * 
	 * <p>
	 * You are getting this "catch-all" property because of the following reason:
	 * The field name "Location" is used by two different parts of a schema. See:
	 * line 717 of file:/Users/laemmel/Documents/workspace/playgrounds/gregor/xsd/INSPIRE/inspire-foss-read-only/schemas/inspire/v3.0.1/CommonTransportElements.xsd
	 * line 26 of http://schemas.opengis.net/gml/3.2.1/feature.xsd
	 * <p>
	 * To get rid of this property, apply a property customization to one
	 * of both of the following declarations to change their names:
	 * Gets the value of the rest property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the rest property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getRest().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
	 * {@link JAXBElement }{@code <}{@link LengthType }{@code >}
	 * 
	 * 
	 */
	public List<JAXBElement<?>> getRest() {
		if (this.rest == null) {
			this.rest = new ArrayList<JAXBElement<?>>();
		}
		return this.rest;
	}

	@Override
	public Object createNewInstance() {
		// TODO Auto-generated method stub
		return null;
	}

}
