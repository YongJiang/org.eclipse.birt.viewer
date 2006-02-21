/**
 * ColumnDefinition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Sep 06, 2005 (12:48:20 PDT) WSDL2Java emitter.
 */

package org.eclipse.birt.report.soapengine.api;

public class ColumnDefinition  implements java.io.Serializable {
    private int index;
    private java.lang.String header;
    private java.lang.String expr;
    private java.lang.Integer newIndex;
    private java.lang.Boolean isGrouped;
    private org.eclipse.birt.report.soapengine.api.SortingDirection sortDir;
    private org.eclipse.birt.report.soapengine.api.AggregateDefinition aggregate;
    private org.eclipse.birt.report.soapengine.api.Font font;
    private org.eclipse.birt.report.soapengine.api.Format format;

    public ColumnDefinition() {
    }

    public ColumnDefinition(
           int index,
           java.lang.String header,
           java.lang.String expr,
           java.lang.Integer newIndex,
           java.lang.Boolean isGrouped,
           org.eclipse.birt.report.soapengine.api.SortingDirection sortDir,
           org.eclipse.birt.report.soapengine.api.AggregateDefinition aggregate,
           org.eclipse.birt.report.soapengine.api.Font font,
           org.eclipse.birt.report.soapengine.api.Format format) {
           this.index = index;
           this.header = header;
           this.expr = expr;
           this.newIndex = newIndex;
           this.isGrouped = isGrouped;
           this.sortDir = sortDir;
           this.aggregate = aggregate;
           this.font = font;
           this.format = format;
    }


    /**
     * Gets the index value for this ColumnDefinition.
     * 
     * @return index
     */
    public int getIndex() {
        return index;
    }


    /**
     * Sets the index value for this ColumnDefinition.
     * 
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }


    /**
     * Gets the header value for this ColumnDefinition.
     * 
     * @return header
     */
    public java.lang.String getHeader() {
        return header;
    }


    /**
     * Sets the header value for this ColumnDefinition.
     * 
     * @param header
     */
    public void setHeader(java.lang.String header) {
        this.header = header;
    }


    /**
     * Gets the expr value for this ColumnDefinition.
     * 
     * @return expr
     */
    public java.lang.String getExpr() {
        return expr;
    }


    /**
     * Sets the expr value for this ColumnDefinition.
     * 
     * @param expr
     */
    public void setExpr(java.lang.String expr) {
        this.expr = expr;
    }


    /**
     * Gets the newIndex value for this ColumnDefinition.
     * 
     * @return newIndex
     */
    public java.lang.Integer getNewIndex() {
        return newIndex;
    }


    /**
     * Sets the newIndex value for this ColumnDefinition.
     * 
     * @param newIndex
     */
    public void setNewIndex(java.lang.Integer newIndex) {
        this.newIndex = newIndex;
    }


    /**
     * Gets the isGrouped value for this ColumnDefinition.
     * 
     * @return isGrouped
     */
    public java.lang.Boolean getIsGrouped() {
        return isGrouped;
    }


    /**
     * Sets the isGrouped value for this ColumnDefinition.
     * 
     * @param isGrouped
     */
    public void setIsGrouped(java.lang.Boolean isGrouped) {
        this.isGrouped = isGrouped;
    }


    /**
     * Gets the sortDir value for this ColumnDefinition.
     * 
     * @return sortDir
     */
    public org.eclipse.birt.report.soapengine.api.SortingDirection getSortDir() {
        return sortDir;
    }


    /**
     * Sets the sortDir value for this ColumnDefinition.
     * 
     * @param sortDir
     */
    public void setSortDir(org.eclipse.birt.report.soapengine.api.SortingDirection sortDir) {
        this.sortDir = sortDir;
    }


    /**
     * Gets the aggregate value for this ColumnDefinition.
     * 
     * @return aggregate
     */
    public org.eclipse.birt.report.soapengine.api.AggregateDefinition getAggregate() {
        return aggregate;
    }


    /**
     * Sets the aggregate value for this ColumnDefinition.
     * 
     * @param aggregate
     */
    public void setAggregate(org.eclipse.birt.report.soapengine.api.AggregateDefinition aggregate) {
        this.aggregate = aggregate;
    }


    /**
     * Gets the font value for this ColumnDefinition.
     * 
     * @return font
     */
    public org.eclipse.birt.report.soapengine.api.Font getFont() {
        return font;
    }


    /**
     * Sets the font value for this ColumnDefinition.
     * 
     * @param font
     */
    public void setFont(org.eclipse.birt.report.soapengine.api.Font font) {
        this.font = font;
    }


    /**
     * Gets the format value for this ColumnDefinition.
     * 
     * @return format
     */
    public org.eclipse.birt.report.soapengine.api.Format getFormat() {
        return format;
    }


    /**
     * Sets the format value for this ColumnDefinition.
     * 
     * @param format
     */
    public void setFormat(org.eclipse.birt.report.soapengine.api.Format format) {
        this.format = format;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ColumnDefinition)) return false;
        ColumnDefinition other = (ColumnDefinition) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.index == other.getIndex() &&
            ((this.header==null && other.getHeader()==null) || 
             (this.header!=null &&
              this.header.equals(other.getHeader()))) &&
            ((this.expr==null && other.getExpr()==null) || 
             (this.expr!=null &&
              this.expr.equals(other.getExpr()))) &&
            ((this.newIndex==null && other.getNewIndex()==null) || 
             (this.newIndex!=null &&
              this.newIndex.equals(other.getNewIndex()))) &&
            ((this.isGrouped==null && other.getIsGrouped()==null) || 
             (this.isGrouped!=null &&
              this.isGrouped.equals(other.getIsGrouped()))) &&
            ((this.sortDir==null && other.getSortDir()==null) || 
             (this.sortDir!=null &&
              this.sortDir.equals(other.getSortDir()))) &&
            ((this.aggregate==null && other.getAggregate()==null) || 
             (this.aggregate!=null &&
              this.aggregate.equals(other.getAggregate()))) &&
            ((this.font==null && other.getFont()==null) || 
             (this.font!=null &&
              this.font.equals(other.getFont()))) &&
            ((this.format==null && other.getFormat()==null) || 
             (this.format!=null &&
              this.format.equals(other.getFormat())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getIndex();
        if (getHeader() != null) {
            _hashCode += getHeader().hashCode();
        }
        if (getExpr() != null) {
            _hashCode += getExpr().hashCode();
        }
        if (getNewIndex() != null) {
            _hashCode += getNewIndex().hashCode();
        }
        if (getIsGrouped() != null) {
            _hashCode += getIsGrouped().hashCode();
        }
        if (getSortDir() != null) {
            _hashCode += getSortDir().hashCode();
        }
        if (getAggregate() != null) {
            _hashCode += getAggregate().hashCode();
        }
        if (getFont() != null) {
            _hashCode += getFont().hashCode();
        }
        if (getFormat() != null) {
            _hashCode += getFormat().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ColumnDefinition.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "ColumnDefinition"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("index");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "Index"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("header");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "Header"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("expr");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "Expr"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("newIndex");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "NewIndex"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isGrouped");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "IsGrouped"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sortDir");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "SortDir"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "SortingDirection"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aggregate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "Aggregate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "AggregateDefinition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("font");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "Font"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "Font"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("format");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "Format"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.eclipse.org/birt", "Format"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
