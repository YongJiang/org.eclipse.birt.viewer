/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.viewer.browsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.birt.report.viewer.ViewerPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.browser.IBrowserFactory;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.HelpBaseResources;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Singlton class manage registered browsers.
 * 
 * @version $
 */
public class BrowserManager
{
	/**
	 * Preference key for always using external browser
	 */
	public static final String ALWAYS_EXTERNAL_BROWSER_KEY = "always_external_browser"; //$NON-NLS-1$

	/**
	 * Preference key for default browser
	 */
	public static final String DEFAULT_BROWSER_ID_KEY = "default_browser"; //$NON-NLS-1$

	/**
	 * Customer browser id
	 */
	public static final String BROWSER_ID_CUSTOM = ViewerPlugin.PLUGIN_ID + ".custombrowser"; //$NON-NLS-1$

	/**
	 * Embedded browser id
	 */
	public static final String BROWSER_ID_EMBEDDED = ViewerPlugin.PLUGIN_ID + ".embeddedbrowser";

	/**
	 * System browser id
	 */
	public static final String BROWSER_ID_SYSTEM = ViewerPlugin.PLUGIN_ID + ".systembrowser";

	/**
	 * Mozilla browser id
	 */
	public static final String BROWSER_ID_MOZILLA = HelpBasePlugin.PLUGIN_ID + ".mozilla"; //$NON-NLS-1$

	/**
	 * Netscape browser id
	 */
	public static final String BROWSER_ID_NETSCAPE = HelpBasePlugin.PLUGIN_ID + ".netscape"; //$NON-NLS-1$

	/**
	 * Default MacOS browser id
	 */
	public static final String BROWSER_ID_MAC_SYSTEM = HelpBasePlugin.PLUGIN_ID + ".defaultBrowserMacOSX"; //$NON-NLS-1$

	private static BrowserManager instance;

	private BrowserDescriptor currentBrowserDesc;
	private BrowserDescriptor defaultBrowserDesc;
	private BrowserDescriptor[] browsersDescriptors;
	private BrowserDescriptor internalBrowserDesc;

	private Collection browsers = new ArrayList( );

	private boolean alwaysUseExternal = false;

	/**
	 * Obtains singleton instance.
	 * 
	 * @return browser manager instance
	 */
	public static BrowserManager getInstance( )
	{
		if ( instance == null )
		{
			instance = new BrowserManager( );

			instance.init( );
		}

		return instance;
	}

	/**
	 * Private Constructor
	 */
	private BrowserManager( )
	{
		;
	}

	/**
	 * Initialize
	 */
	private void init( )
	{
		// Find all available browsers
		browsersDescriptors = createBrowserDescriptors( );

		// 1. set default browser from preferences
		String defBrowserID = ViewerPlugin.getDefault( )
				.getPluginPreferences( )
				.getDefaultString( DEFAULT_BROWSER_ID_KEY );

		if ( defBrowserID != null && ( !"".equals( defBrowserID ) ) )
		{
			setDefaultBrowserID( defBrowserID );
		}

		// 2. set default browser to embedded ????
		//		if (defaultBrowserDesc == null) {
		//			setDefaultBrowserID(BROWSER_ID_EMBEDDED);
		//		}
		// 3. set default browser to help implementation of system specific
		// browser
		String os = Platform.getOS( );

		if ( defaultBrowserDesc == null )
		{
			if ( Constants.WS_WIN32.equalsIgnoreCase( os ) )
			{
				// Win32 uses system browser
				setDefaultBrowserID( BROWSER_ID_SYSTEM );
			}
			else if ( Constants.OS_AIX.equalsIgnoreCase( os )
					|| Constants.OS_HPUX.equalsIgnoreCase( os )
					|| Constants.OS_LINUX.equalsIgnoreCase( os )
					|| Constants.OS_SOLARIS.equalsIgnoreCase( os ) )
			{
				// Unix uses mozilla
				setDefaultBrowserID( BROWSER_ID_MOZILLA );

				if ( defaultBrowserDesc == null )
				{
					// Or netscape
					setDefaultBrowserID( BROWSER_ID_NETSCAPE );
				}
			}
			else if ( Constants.OS_MACOSX.equalsIgnoreCase( os ) )
			{
				// Mac
				setDefaultBrowserID( BROWSER_ID_MAC_SYSTEM );
			}
		}

		// 4. set browser to one of externally contributed
		if ( defaultBrowserDesc == null )
		{
			for ( int i = 0; i < browsersDescriptors.length; i++ )
			{
				if ( BROWSER_ID_CUSTOM.equals( browsersDescriptors[i].getID( ) ) )
				{
					defaultBrowserDesc = browsersDescriptors[i];
				}
			}
		}

		// 5. let user specify program
		if ( defaultBrowserDesc == null )
		{
			setDefaultBrowserID( BROWSER_ID_CUSTOM );
		}

		// 6. use null browser
		if ( defaultBrowserDesc == null )
		{
			// If no browsers at all, use the Null Browser Adapter
			defaultBrowserDesc = new BrowserDescriptor( "", "Null Browser", //$NON-NLS-1$ //$NON-NLS-2$
					new IBrowserFactory( ) {

						public boolean isAvailable( )
						{
							return true;
						}

						public IBrowser createBrowser( )
						{
							return new IBrowser( ) {

								public void close( )
								{
								}

								public void displayURL( String url )
								{
									String msg = HelpBaseResources.getString( "no_browsers", url ); //$NON-NLS-1$

									HelpBasePlugin.logError( msg, null );

									BaseHelpSystem.getDefaultErrorUtil( )
											.displayError( msg );
								}

								public boolean isCloseSupported( )
								{
									return false;
								}

								public boolean isSetLocationSupported( )
								{
									return false;
								}

								public boolean isSetSizeSupported( )
								{
									return false;
								}

								public void setLocation( int width, int height )
								{
								}

								public void setSize( int x, int y )
								{
								}
							};
						}
					} );
		}

		// initialize current browser
		String curBrowserID = ViewerPlugin.getDefault( )
				.getPluginPreferences( )
				.getString( DEFAULT_BROWSER_ID_KEY );

		if ( curBrowserID != null && ( !"".equals( curBrowserID ) ) )
		{
			setCurrentBrowserID( curBrowserID );
			// may fail if such browser does not exist
		}

		if ( currentBrowserDesc == null )
		{
			setCurrentBrowserID( getDefaultBrowserID( ) );
		}

		setAlwaysUseExternal( ViewerPlugin.getDefault( )
				.getPluginPreferences( )
				.getBoolean( ALWAYS_EXTERNAL_BROWSER_KEY ) );
	}

	/**
	 * Creates all adapters, and returns available ones.
	 */
	private BrowserDescriptor[] createBrowserDescriptors( )
	{
		if ( browsersDescriptors != null )
		{
			return browsersDescriptors;
		}

		Collection bDescriptors = new ArrayList( );

		IConfigurationElement configElements[] = Platform.getExtensionRegistry( )
				.getConfigurationElementsFor( ViewerPlugin.PLUGIN_ID, "browser" ); //$NON-NLS-1$

		for ( int i = 0; i < configElements.length; i++ )
		{
			if ( !configElements[i].getName( ).equals( "browser" ) ) //$NON-NLS-1$
			{
				continue;
			}

			String id = configElements[i].getAttribute( "id" ); //$NON-NLS-1$

			if ( id == null )
			{
				continue;
			}

			String label = configElements[i].getAttribute( "name" ); //$NON-NLS-1$

			if ( label == null )
			{
				continue;
			}

			try
			{
				Object adapter = configElements[i].createExecutableExtension( "factoryclass" ); //$NON-NLS-1$

				if ( !( adapter instanceof IBrowserFactory ) )
				{
					continue;
				}

				if ( ( (IBrowserFactory) adapter ).isAvailable( ) )
				{
					BrowserDescriptor descriptor = new BrowserDescriptor( id,
							label,
							(IBrowserFactory) adapter );

					if ( descriptor.isExternal( ) )
					{
						bDescriptors.add( descriptor );
					}
					else
					{
						internalBrowserDesc = descriptor;
					}
				}
			}
			catch ( CoreException ce )
			{
				;
			}
		}

		this.browsersDescriptors = (BrowserDescriptor[]) bDescriptors.toArray( new BrowserDescriptor[bDescriptors.size( )] );

		return this.browsersDescriptors;
	}

	/**
	 * Obtains browsers descriptors.
	 * 
	 * @return array of browser descriptors
	 */
	public BrowserDescriptor[] getBrowserDescriptors( )
	{
		return browsersDescriptors;
	}

	/**
	 * Gets the current browser id.
	 * 
	 * @return current browser id
	 */
	public String getCurrentBrowserID( )
	{
		if ( currentBrowserDesc == null )
		{
			return null;
		}

		return currentBrowserDesc.getID( );
	}

	/**
	 * Get the current internal browser id.
	 * 
	 * @return current internal browser id
	 */
	public String getCurrentInternalBrowserID( )
	{
		if ( isEmbeddedBrowserPresent( ) && !alwaysUseExternal )
		{
			return internalBrowserDesc.getID( );
		}
		else
		{
			return getCurrentBrowserID( );
		}
	}

	/**
	 * Gets the deafult browser id.
	 * 
	 * @return default browser id
	 */
	public String getDefaultBrowserID( )
	{
		if ( defaultBrowserDesc == null )
		{
			return null;
		}

		return defaultBrowserDesc.getID( );
	}

	/**
	 * Set current browser id.
	 * 
	 * @param currentAdapterID Id of the current browser
	 */
	public void setCurrentBrowserID( String currentAdapterID )
	{
		for ( int i = 0; i < browsersDescriptors.length; i++ )
		{
			if ( browsersDescriptors[i].getID( ).equals( currentAdapterID ) )
			{
				currentBrowserDesc = browsersDescriptors[i];

				return;
			}
		}
	}

	/**
	 * Set default browser id.
	 * 
	 * @param defaultAdapterID Id of default browser
	 */
	private void setDefaultBrowserID( String defaultAdapterID )
	{
		for ( int i = 0; i < browsersDescriptors.length; i++ )
		{
			if ( browsersDescriptors[i].getID( ).equals( defaultAdapterID ) )
			{
				defaultBrowserDesc = browsersDescriptors[i];

				return;
			}
		}
	}

	/**
	 * Creates web browser.
	 * 
	 * @param external using external browser or not
	 * @return browser instance
	 */
	public IBrowser createBrowser( boolean external )
	{
		if ( external )
		{
			return new CurrentBrowser( createBrowserAdapter( true ),
					getCurrentBrowserID( ),
					true );
		}
		else
		{
			return new CurrentBrowser( createBrowserAdapter( alwaysUseExternal ),
					getCurrentInternalBrowserID( ),
					false );
		}
	}

	/**
	 * Creates web browser.
	 * 
	 * @return browser instance
	 */
	public IBrowser createBrowser( )
	{
		return createBrowser( true );
	}

	/**
	 * Creates web browser adapter.
	 * 
	 * @param external using external browser or not
	 * @return browser instance
	 */
	private IBrowser createBrowserAdapter( boolean external )
	{
		IBrowser browser = null;

		if ( !external && isEmbeddedBrowserPresent( ) )
		{
			browser = internalBrowserDesc.getFactory( ).createBrowser( );
		}
		else
		{
			browser = currentBrowserDesc.getFactory( ).createBrowser( );
		}

		browsers.add( browser );

		return browser;
	}

	/**
	 * Closes all browsers created.
	 */
	public void closeAll( )
	{
		for ( Iterator it = browsers.iterator( ); it.hasNext( ); )
		{
			IBrowser browser = (IBrowser) it.next( );

			browser.close( );
		}
	}

	/**
	 * Is embedded browser present.
	 * 
	 * @return embedded browser present or not
	 */
	public boolean isEmbeddedBrowserPresent( )
	{
		return internalBrowserDesc != null;
	}

	/**
	 * Set always using external browser.
	 * 
	 * @param alwaysExternal always using external browser
	 */
	public void setAlwaysUseExternal( boolean alwaysExternal )
	{
		alwaysUseExternal = alwaysExternal || !isEmbeddedBrowserPresent( );
	}

	/**
	 * Is always using external browser.
	 * 
	 * @return always using external browser or not
	 */
	private boolean isAlwaysUseExternal( )
	{
		if ( !isEmbeddedBrowserPresent( ) )
		{
			return true;
		}

		return alwaysUseExternal;
	}
}