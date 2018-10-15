package org.openmrs.module.mentalhealth.utils;

public class MhColumnParameters {

    private String name;

    private String label;

    private String dimensions;

    /**
     * Default constructor
     *
     * @param name the name
     * @param label the label
     * @param dimensions the dimension parameters
     */
    public MhColumnParameters(String name, String label, String dimensions) {
        this.name = name;
        this.label = label;
        this.dimensions = dimensions;
    }

    /**
     * Gets the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the label
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the dimension parameters
     *
     * @return the dimension parameters
     */
    public String getDimensions() {
        return dimensions;
    }
}
