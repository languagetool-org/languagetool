<?php
/*************************************************************************************
 * java.php
 * --------
 * Author: Nigel McNie (nigel@geshi.org)
 * Copyright: (c) 2004 Nigel McNie (http://qbnz.com/highlighter/)
 * Release Version: 1.0.7.20
 * Date Started: 2004/07/10
 *
 * Java language file for GeSHi.
 *
 * CHANGES
 * -------
 * 2005/12/28 (1.0.4)
 *   -  Added instanceof keyword
 * 2004/11/27 (1.0.3)
 *   -  Added support for multiple object splitters
 * 2004/08/05 (1.0.2)
 *   -  Added URL support
 *   -  Added keyword "this", as bugs in GeSHi class ironed out
 * 2004/08/05 (1.0.1)
 *   -  Added support for symbols
 *   -  Added extra missed keywords
 * 2004/07/14 (1.0.0)
 *   -  First Release
 *
 * TODO
 * -------------------------
 * *
 *
 *************************************************************************************
 *
 *     This file is part of GeSHi.
 *
 *   GeSHi is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   GeSHi is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GeSHi; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 ************************************************************************************/

$language_data = array (
	'LANG_NAME' => 'Java(TM) 2 Platform Standard Edition 5.0',
	'COMMENT_SINGLE' => array(1 => '//'),   /* import statements are not comments! */
	'COMMENT_MULTI' => array('/*' => '*/'),
	'CASE_KEYWORDS' => GESHI_CAPS_NO_CHANGE,
	'QUOTEMARKS' => array("'", '"'),
	'ESCAPE_CHAR' => '\\',
	'KEYWORDS' => array(
		1 => array(
			/* see the authoritative list of all 50 Java keywords at */
			/* http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#229308 */

			/* java keywords, part 1: control flow */
			'case', 'default', 'do', 'else', 'for',
			'goto', 'if', 'switch', 'while'

			/* IMO 'break', 'continue', 'return' and 'throw' */
                        /* should also be added to this group, as they   */
			/* also manage the control flow,                 */
			/* arguably 'try'/'catch'/'finally' as well      */
			),
		2 => array(
			/* java keywords, part 2 */

			'break', 'continue', 'return', 'throw',
			'try', 'catch', 'finally',

			'abstract', 'assert', 'class', 'const', 'enum', 'extends',
			'final', 'implements', 'import', 'instanceof', 'interface',
			'native', 'new', 'package', 'private', 'protected',
			'public', 'static', 'strictfp', 'super', 'synchronized',
			'this', 'throws', 'transient', 'volatile'
			),
		3 => array(
			/* Java keywords, part 3: primitive data types and 'void' */
			'boolean', 'byte', 'char', 'double',
			'float', 'int', 'long', 'short', 'void'
			),
		4 => array(
			/* other reserved words in Java: literals */
			/* should be styled to look similar to numbers and Strings */
			'false', 'null', 'true'
			),
		5 => array (
			'Applet', 'AppletContext', 'AppletStub', 'AudioClip'
			),
		6 => array (
			'AWTError', 'AWTEvent', 'AWTEventMulticaster', 'AWTException', 'AWTKeyStroke', 'AWTPermission', 'ActiveEvent', 'Adjustable', 'AlphaComposite', 'BasicStroke', 'BorderLayout', 'BufferCapabilities', 'BufferCapabilities.FlipContents', 'Button', 'Canvas', 'CardLayout', 'Checkbox', 'CheckboxGroup', 'CheckboxMenuItem', 'Choice', 'Color', 'Component', 'ComponentOrientation', 'Composite', 'CompositeContext', 'Container', 'ContainerOrderFocusTraversalPolicy', 'Cursor', 'DefaultFocusTraversalPolicy', 'DefaultKeyboardFocusManager', 'Dialog', 'Dimension', 'DisplayMode', 'EventQueue', 'FileDialog', 'FlowLayout', 'FocusTraversalPolicy', 'Font', 'FontFormatException', 'FontMetrics', 'Frame', 'GradientPaint', 'Graphics', 'Graphics2D', 'GraphicsConfigTemplate', 'GraphicsConfiguration', 'GraphicsDevice', 'GraphicsEnvironment', 'GridBagConstraints', 'GridBagLayout', 'GridLayout', 'HeadlessException', 'IllegalComponentStateException', 'Image', 'ImageCapabilities', 'Insets', 'ItemSelectable', 'JobAttributes',
			'JobAttributes.DefaultSelectionType', 'JobAttributes.DestinationType', 'JobAttributes.DialogType', 'JobAttributes.MultipleDocumentHandlingType', 'JobAttributes.SidesType', 'KeyEventDispatcher', 'KeyEventPostProcessor', 'KeyboardFocusManager', 'Label', 'LayoutManager', 'LayoutManager2', 'MediaTracker', 'Menu', 'MenuBar', 'MenuComponent', 'MenuContainer', 'MenuItem', 'MenuShortcut', 'MouseInfo', 'PageAttributes', 'PageAttributes.ColorType', 'PageAttributes.MediaType', 'PageAttributes.OrientationRequestedType', 'PageAttributes.OriginType', 'PageAttributes.PrintQualityType', 'Paint', 'PaintContext', 'Panel', 'Point', 'PointerInfo', 'Polygon', 'PopupMenu', 'PrintGraphics', 'PrintJob', 'Rectangle', 'RenderingHints', 'RenderingHints.Key', 'Robot', 'ScrollPane', 'ScrollPaneAdjustable', 'Scrollbar', 'Shape', 'Stroke', 'SystemColor', 'TextArea', 'TextComponent', 'TextField', 'TexturePaint', 'Toolkit', 'Transparency', 'Window'
			),
		7 => array (
			'CMMException', 'ColorSpace', 'ICC_ColorSpace', 'ICC_Profile', 'ICC_ProfileGray', 'ICC_ProfileRGB', 'ProfileDataException'
			),
		8 => array (
			'Clipboard', 'ClipboardOwner', 'DataFlavor', 'FlavorEvent', 'FlavorListener', 'FlavorMap', 'FlavorTable', 'MimeTypeParseException', 'StringSelection', 'SystemFlavorMap', 'Transferable', 'UnsupportedFlavorException'
			),
		9 => array (
			'Autoscroll', 'DnDConstants', 'DragGestureEvent', 'DragGestureListener', 'DragGestureRecognizer', 'DragSource', 'DragSourceAdapter', 'DragSourceContext', 'DragSourceDragEvent', 'DragSourceDropEvent', 'DragSourceEvent', 'DragSourceListener', 'DragSourceMotionListener', 'DropTarget', 'DropTarget.DropTargetAutoScroller', 'DropTargetAdapter', 'DropTargetContext', 'DropTargetDragEvent', 'DropTargetDropEvent', 'DropTargetEvent', 'DropTargetListener', 'InvalidDnDOperationException', 'MouseDragGestureRecognizer'
			),
		10 => array (
			'AWTEventListener', 'AWTEventListenerProxy', 'ActionEvent', 'ActionListener', 'AdjustmentEvent', 'AdjustmentListener', 'ComponentAdapter', 'ComponentEvent', 'ComponentListener', 'ContainerAdapter', 'ContainerEvent', 'ContainerListener', 'FocusAdapter', 'FocusEvent', 'FocusListener', 'HierarchyBoundsAdapter', 'HierarchyBoundsListener', 'HierarchyEvent', 'HierarchyListener', 'InputEvent', 'InputMethodEvent', 'InputMethodListener', 'InvocationEvent', 'ItemEvent', 'ItemListener', 'KeyAdapter', 'KeyEvent', 'KeyListener', 'MouseAdapter', 'MouseListener', 'MouseMotionAdapter', 'MouseMotionListener', 'MouseWheelEvent', 'MouseWheelListener', 'PaintEvent', 'TextEvent', 'TextListener', 'WindowAdapter', 'WindowEvent', 'WindowFocusListener', 'WindowListener', 'WindowStateListener'
			),
		11 => array (
			'FontRenderContext', 'GlyphJustificationInfo', 'GlyphMetrics', 'GlyphVector', 'GraphicAttribute', 'ImageGraphicAttribute', 'LineBreakMeasurer', 'LineMetrics', 'MultipleMaster', 'NumericShaper', 'ShapeGraphicAttribute', 'TextAttribute', 'TextHitInfo', 'TextLayout', 'TextLayout.CaretPolicy', 'TextMeasurer', 'TransformAttribute'
			),
		12 => array (
			'AffineTransform', 'Arc2D', 'Arc2D.Double', 'Arc2D.Float', 'Area', 'CubicCurve2D', 'CubicCurve2D.Double', 'CubicCurve2D.Float', 'Dimension2D', 'Ellipse2D', 'Ellipse2D.Double', 'Ellipse2D.Float', 'FlatteningPathIterator', 'GeneralPath', 'IllegalPathStateException', 'Line2D', 'Line2D.Double', 'Line2D.Float', 'NoninvertibleTransformException', 'PathIterator', 'Point2D', 'Point2D.Double', 'Point2D.Float', 'QuadCurve2D', 'QuadCurve2D.Double', 'QuadCurve2D.Float', 'Rectangle2D', 'Rectangle2D.Double', 'Rectangle2D.Float', 'RectangularShape', 'RoundRectangle2D', 'RoundRectangle2D.Double', 'RoundRectangle2D.Float'
			),
		13 => array (
			'InputContext', 'InputMethodHighlight', 'InputMethodRequests', 'InputSubset'
			),
		14 => array (
			'InputMethod', 'InputMethodContext', 'InputMethodDescriptor'
			),
		15 => array (
			'AffineTransformOp', 'AreaAveragingScaleFilter', 'BandCombineOp', 'BandedSampleModel', 'BufferStrategy', 'BufferedImage', 'BufferedImageFilter', 'BufferedImageOp', 'ByteLookupTable', 'ColorConvertOp', 'ColorModel', 'ComponentColorModel', 'ComponentSampleModel', 'ConvolveOp', 'CropImageFilter', 'DataBuffer', 'DataBufferByte', 'DataBufferDouble', 'DataBufferFloat', 'DataBufferInt', 'DataBufferShort', 'DataBufferUShort', 'DirectColorModel', 'FilteredImageSource', 'ImageConsumer', 'ImageFilter', 'ImageObserver', 'ImageProducer', 'ImagingOpException', 'IndexColorModel', 'Kernel', 'LookupOp', 'LookupTable', 'MemoryImageSource', 'MultiPixelPackedSampleModel', 'PackedColorModel', 'PixelGrabber', 'PixelInterleavedSampleModel', 'RGBImageFilter', 'Raster', 'RasterFormatException', 'RasterOp', 'RenderedImage', 'ReplicateScaleFilter', 'RescaleOp', 'SampleModel', 'ShortLookupTable', 'SinglePixelPackedSampleModel', 'TileObserver', 'VolatileImage', 'WritableRaster', 'WritableRenderedImage'
			),
		16 => array (
			'ContextualRenderedImageFactory', 'ParameterBlock', 'RenderContext', 'RenderableImage', 'RenderableImageOp', 'RenderableImageProducer', 'RenderedImageFactory'
			),
		17 => array (
			'Book', 'PageFormat', 'Pageable', 'Paper', 'Printable', 'PrinterAbortException', 'PrinterException', 'PrinterGraphics', 'PrinterIOException', 'PrinterJob'
			),
		18 => array (
			'AppletInitializer', 'BeanDescriptor', 'BeanInfo', 'Beans', 'Customizer', 'DefaultPersistenceDelegate', 'DesignMode', 'Encoder', 'EventHandler', 'EventSetDescriptor', 'ExceptionListener', 'Expression', 'FeatureDescriptor', 'IndexedPropertyChangeEvent', 'IndexedPropertyDescriptor', 'Introspector', 'MethodDescriptor', 'ParameterDescriptor', 'PersistenceDelegate', 'PropertyChangeEvent', 'PropertyChangeListener', 'PropertyChangeListenerProxy', 'PropertyChangeSupport', 'PropertyDescriptor', 'PropertyEditor', 'PropertyEditorManager', 'PropertyEditorSupport', 'PropertyVetoException', 'SimpleBeanInfo', 'VetoableChangeListener', 'VetoableChangeListenerProxy', 'VetoableChangeSupport', 'Visibility', 'XMLDecoder', 'XMLEncoder'
			),
		19 => array (
			'BeanContext', 'BeanContextChild', 'BeanContextChildComponentProxy', 'BeanContextChildSupport', 'BeanContextContainerProxy', 'BeanContextEvent', 'BeanContextMembershipEvent', 'BeanContextMembershipListener', 'BeanContextProxy', 'BeanContextServiceAvailableEvent', 'BeanContextServiceProvider', 'BeanContextServiceProviderBeanInfo', 'BeanContextServiceRevokedEvent', 'BeanContextServiceRevokedListener', 'BeanContextServices', 'BeanContextServicesListener', 'BeanContextServicesSupport', 'BeanContextServicesSupport.BCSSServiceProvider', 'BeanContextSupport', 'BeanContextSupport.BCSIterator'
			),
		20 => array (
			'BufferedInputStream', 'BufferedOutputStream', 'BufferedReader', 'BufferedWriter', 'ByteArrayInputStream', 'ByteArrayOutputStream', 'CharArrayReader', 'CharArrayWriter', 'CharConversionException', 'Closeable', 'DataInput', 'DataOutput', 'EOFException', 'Externalizable', 'File', 'FileDescriptor', 'FileInputStream', 'FileNotFoundException', 'FileOutputStream', 'FilePermission', 'FileReader', 'FileWriter', 'FilenameFilter', 'FilterInputStream', 'FilterOutputStream', 'FilterReader', 'FilterWriter', 'Flushable', 'IOException', 'InputStreamReader', 'InterruptedIOException', 'InvalidClassException', 'InvalidObjectException', 'LineNumberInputStream', 'LineNumberReader', 'NotActiveException', 'NotSerializableException', 'ObjectInput', 'ObjectInputStream', 'ObjectInputStream.GetField', 'ObjectInputValidation', 'ObjectOutput', 'ObjectOutputStream', 'ObjectOutputStream.PutField', 'ObjectStreamClass', 'ObjectStreamConstants', 'ObjectStreamException', 'ObjectStreamField', 'OptionalDataException', 'OutputStreamWriter',
			'PipedInputStream', 'PipedOutputStream', 'PipedReader', 'PipedWriter', 'PrintStream', 'PrintWriter', 'PushbackInputStream', 'PushbackReader', 'RandomAccessFile', 'Reader', 'SequenceInputStream', 'Serializable', 'SerializablePermission', 'StreamCorruptedException', 'StreamTokenizer', 'StringBufferInputStream', 'StringReader', 'StringWriter', 'SyncFailedException', 'UTFDataFormatException', 'UnsupportedEncodingException', 'WriteAbortedException', 'Writer'
			),
		21 => array (
			'AbstractMethodError', 'Appendable', 'ArithmeticException', 'ArrayIndexOutOfBoundsException', 'ArrayStoreException', 'AssertionError', 'Boolean', 'Byte', 'CharSequence', 'Character', 'Character.Subset', 'Character.UnicodeBlock', 'Class', 'ClassCastException', 'ClassCircularityError', 'ClassFormatError', 'ClassLoader', 'ClassNotFoundException', 'CloneNotSupportedException', 'Cloneable', 'Comparable', 'Compiler', 'Deprecated', 'Double', 'Enum', 'EnumConstantNotPresentException', 'Error', 'Exception', 'ExceptionInInitializerError', 'Float', 'IllegalAccessError', 'IllegalAccessException', 'IllegalArgumentException', 'IllegalMonitorStateException', 'IllegalStateException', 'IllegalThreadStateException', 'IncompatibleClassChangeError', 'IndexOutOfBoundsException', 'InheritableThreadLocal', 'InstantiationError', 'InstantiationException', 'Integer', 'InternalError', 'InterruptedException', 'Iterable', 'LinkageError', 'Long', 'Math', 'NegativeArraySizeException', 'NoClassDefFoundError', 'NoSuchFieldError',
			'NoSuchFieldException', 'NoSuchMethodError', 'NoSuchMethodException', 'NullPointerException', 'Number', 'NumberFormatException', 'OutOfMemoryError', 'Override', 'Package', 'Process', 'ProcessBuilder', 'Readable', 'Runnable', 'Runtime', 'RuntimeException', 'RuntimePermission', 'SecurityException', 'SecurityManager', 'Short', 'StackOverflowError', 'StackTraceElement', 'StrictMath', 'String', 'StringBuffer', 'StringBuilder', 'StringIndexOutOfBoundsException', 'SuppressWarnings', 'System', 'Thread', 'Thread.State', 'Thread.UncaughtExceptionHandler', 'ThreadDeath', 'ThreadGroup', 'ThreadLocal', 'Throwable', 'TypeNotPresentException', 'UnknownError', 'UnsatisfiedLinkError', 'UnsupportedClassVersionError', 'UnsupportedOperationException', 'VerifyError', 'VirtualMachineError', 'Void'
			),
		22 => array (
			'AnnotationFormatError', 'AnnotationTypeMismatchException', 'Documented', 'ElementType', 'IncompleteAnnotationException', 'Inherited', 'Retention', 'RetentionPolicy', 'Target'
			),
		23 => array (
			'ClassDefinition', 'ClassFileTransformer', 'IllegalClassFormatException', 'Instrumentation', 'UnmodifiableClassException'
			),
		24 => array (
			'ClassLoadingMXBean', 'CompilationMXBean', 'GarbageCollectorMXBean', 'ManagementFactory', 'ManagementPermission', 'MemoryMXBean', 'MemoryManagerMXBean', 'MemoryNotificationInfo', 'MemoryPoolMXBean', 'MemoryType', 'MemoryUsage', 'OperatingSystemMXBean', 'RuntimeMXBean', 'ThreadInfo', 'ThreadMXBean'
			),
		25 => array (
			'PhantomReference', 'ReferenceQueue', 'SoftReference', 'WeakReference'
			),
		26 => array (
			'AccessibleObject', 'AnnotatedElement', 'Constructor', 'Field', 'GenericArrayType', 'GenericDeclaration', 'GenericSignatureFormatError', 'InvocationHandler', 'InvocationTargetException', 'MalformedParameterizedTypeException', 'Member', 'Method', 'Modifier', 'ParameterizedType', 'ReflectPermission', 'Type', 'TypeVariable', 'UndeclaredThrowableException', 'WildcardType'
			),
		27 => array (
			'BigDecimal', 'BigInteger', 'MathContext', 'RoundingMode'
			),
		28 => array (
			'Authenticator', 'Authenticator.RequestorType', 'BindException', 'CacheRequest', 'CacheResponse', 'ContentHandlerFactory', 'CookieHandler', 'DatagramPacket', 'DatagramSocket', 'DatagramSocketImpl', 'DatagramSocketImplFactory', 'FileNameMap', 'HttpRetryException', 'HttpURLConnection', 'Inet4Address', 'Inet6Address', 'InetAddress', 'InetSocketAddress', 'JarURLConnection', 'MalformedURLException', 'MulticastSocket', 'NetPermission', 'NetworkInterface', 'NoRouteToHostException', 'PasswordAuthentication', 'PortUnreachableException', 'ProtocolException', 'Proxy.Type', 'ProxySelector', 'ResponseCache', 'SecureCacheResponse', 'ServerSocket', 'Socket', 'SocketAddress', 'SocketException', 'SocketImpl', 'SocketImplFactory', 'SocketOptions', 'SocketPermission', 'SocketTimeoutException', 'URI', 'URISyntaxException', 'URL', 'URLClassLoader', 'URLConnection', 'URLDecoder', 'URLEncoder', 'URLStreamHandler', 'URLStreamHandlerFactory', 'UnknownServiceException'
			),
		29 => array (
			'Buffer', 'BufferOverflowException', 'BufferUnderflowException', 'ByteBuffer', 'ByteOrder', 'CharBuffer', 'DoubleBuffer', 'FloatBuffer', 'IntBuffer', 'InvalidMarkException', 'LongBuffer', 'MappedByteBuffer', 'ReadOnlyBufferException', 'ShortBuffer'
			),
		30 => array (
			'AlreadyConnectedException', 'AsynchronousCloseException', 'ByteChannel', 'CancelledKeyException', 'Channel', 'Channels', 'ClosedByInterruptException', 'ClosedChannelException', 'ClosedSelectorException', 'ConnectionPendingException', 'DatagramChannel', 'FileChannel', 'FileChannel.MapMode', 'FileLock', 'FileLockInterruptionException', 'GatheringByteChannel', 'IllegalBlockingModeException', 'IllegalSelectorException', 'InterruptibleChannel', 'NoConnectionPendingException', 'NonReadableChannelException', 'NonWritableChannelException', 'NotYetBoundException', 'NotYetConnectedException', 'OverlappingFileLockException', 'Pipe', 'Pipe.SinkChannel', 'Pipe.SourceChannel', 'ReadableByteChannel', 'ScatteringByteChannel', 'SelectableChannel', 'SelectionKey', 'Selector', 'ServerSocketChannel', 'SocketChannel', 'UnresolvedAddressException', 'UnsupportedAddressTypeException', 'WritableByteChannel'
			),
		31 => array (
			'AbstractInterruptibleChannel', 'AbstractSelectableChannel', 'AbstractSelectionKey', 'AbstractSelector', 'SelectorProvider'
			),
		32 => array (
			'CharacterCodingException', 'Charset', 'CharsetDecoder', 'CharsetEncoder', 'CoderMalfunctionError', 'CoderResult', 'CodingErrorAction', 'IllegalCharsetNameException', 'MalformedInputException', 'UnmappableCharacterException', 'UnsupportedCharsetException'
			),
		33 => array (
			'CharsetProvider'
			),
		34 => array (
			'AccessException', 'AlreadyBoundException', 'ConnectIOException', 'MarshalException', 'MarshalledObject', 'Naming', 'NoSuchObjectException', 'NotBoundException', 'RMISecurityException', 'RMISecurityManager', 'Remote', 'RemoteException', 'ServerError', 'ServerException', 'ServerRuntimeException', 'StubNotFoundException', 'UnexpectedException', 'UnmarshalException'
			),
		35 => array (
			'Activatable', 'ActivateFailedException', 'ActivationDesc', 'ActivationException', 'ActivationGroup', 'ActivationGroupDesc', 'ActivationGroupDesc.CommandEnvironment', 'ActivationGroupID', 'ActivationGroup_Stub', 'ActivationID', 'ActivationInstantiator', 'ActivationMonitor', 'ActivationSystem', 'Activator', 'UnknownGroupException', 'UnknownObjectException'
			),
		36 => array (
			'DGC', 'Lease', 'VMID'
			),
		37 => array (
			'LocateRegistry', 'Registry', 'RegistryHandler'
			),
		38 => array (
			'ExportException', 'LoaderHandler', 'LogStream', 'ObjID', 'Operation', 'RMIClassLoader', 'RMIClassLoaderSpi', 'RMIClientSocketFactory', 'RMIFailureHandler', 'RMIServerSocketFactory', 'RMISocketFactory', 'RemoteCall', 'RemoteObject', 'RemoteObjectInvocationHandler', 'RemoteRef', 'RemoteServer', 'RemoteStub', 'ServerCloneException', 'ServerNotActiveException', 'ServerRef', 'Skeleton', 'SkeletonMismatchException', 'SkeletonNotFoundException', 'SocketSecurityException', 'UID', 'UnicastRemoteObject', 'Unreferenced'
			),
		39 => array (
			'AccessControlContext', 'AccessControlException', 'AccessController', 'AlgorithmParameterGenerator', 'AlgorithmParameterGeneratorSpi', 'AlgorithmParameters', 'AlgorithmParametersSpi', 'AllPermission', 'AuthProvider', 'BasicPermission', 'CodeSigner', 'CodeSource', 'DigestException', 'DigestInputStream', 'DigestOutputStream', 'DomainCombiner', 'GeneralSecurityException', 'Guard', 'GuardedObject', 'Identity', 'IdentityScope', 'InvalidAlgorithmParameterException', 'InvalidParameterException', 'Key', 'KeyException', 'KeyFactory', 'KeyFactorySpi', 'KeyManagementException', 'KeyPair', 'KeyPairGenerator', 'KeyPairGeneratorSpi', 'KeyRep', 'KeyRep.Type', 'KeyStore', 'KeyStore.Builder', 'KeyStore.CallbackHandlerProtection', 'KeyStore.Entry', 'KeyStore.LoadStoreParameter', 'KeyStore.PasswordProtection', 'KeyStore.PrivateKeyEntry', 'KeyStore.ProtectionParameter', 'KeyStore.SecretKeyEntry', 'KeyStore.TrustedCertificateEntry', 'KeyStoreException', 'KeyStoreSpi', 'MessageDigest', 'MessageDigestSpi',
			'NoSuchAlgorithmException', 'NoSuchProviderException', 'PermissionCollection', 'Permissions', 'PrivateKey', 'PrivilegedAction', 'PrivilegedActionException', 'PrivilegedExceptionAction', 'ProtectionDomain', 'Provider', 'Provider.Service', 'ProviderException', 'PublicKey', 'SecureClassLoader', 'SecureRandom', 'SecureRandomSpi', 'Security', 'SecurityPermission', 'Signature', 'SignatureException', 'SignatureSpi', 'SignedObject', 'Signer', 'UnrecoverableEntryException', 'UnrecoverableKeyException', 'UnresolvedPermission'
			),
		40 => array (
			'Acl', 'AclEntry', 'AclNotFoundException', 'Group', 'LastOwnerException', 'NotOwnerException', 'Owner'
			),
		41 => array (
			'CRL', 'CRLException', 'CRLSelector', 'CertPath', 'CertPath.CertPathRep', 'CertPathBuilder', 'CertPathBuilderException', 'CertPathBuilderResult', 'CertPathBuilderSpi', 'CertPathParameters', 'CertPathValidator', 'CertPathValidatorException', 'CertPathValidatorResult', 'CertPathValidatorSpi', 'CertSelector', 'CertStore', 'CertStoreException', 'CertStoreParameters', 'CertStoreSpi', 'Certificate.CertificateRep', 'CertificateFactory', 'CertificateFactorySpi', 'CollectionCertStoreParameters', 'LDAPCertStoreParameters', 'PKIXBuilderParameters', 'PKIXCertPathBuilderResult', 'PKIXCertPathChecker', 'PKIXCertPathValidatorResult', 'PKIXParameters', 'PolicyNode', 'PolicyQualifierInfo', 'TrustAnchor', 'X509CRL', 'X509CRLEntry', 'X509CRLSelector', 'X509CertSelector', 'X509Extension'
			),
		42 => array (
			'DSAKey', 'DSAKeyPairGenerator', 'DSAParams', 'DSAPrivateKey', 'DSAPublicKey', 'ECKey', 'ECPrivateKey', 'ECPublicKey', 'RSAKey', 'RSAMultiPrimePrivateCrtKey', 'RSAPrivateCrtKey', 'RSAPrivateKey', 'RSAPublicKey'
			),
		43 => array (
			'AlgorithmParameterSpec', 'DSAParameterSpec', 'DSAPrivateKeySpec', 'DSAPublicKeySpec', 'ECField', 'ECFieldF2m', 'ECFieldFp', 'ECGenParameterSpec', 'ECParameterSpec', 'ECPoint', 'ECPrivateKeySpec', 'ECPublicKeySpec', 'EllipticCurve', 'EncodedKeySpec', 'InvalidKeySpecException', 'InvalidParameterSpecException', 'KeySpec', 'MGF1ParameterSpec', 'PKCS8EncodedKeySpec', 'PSSParameterSpec', 'RSAKeyGenParameterSpec', 'RSAMultiPrimePrivateCrtKeySpec', 'RSAOtherPrimeInfo', 'RSAPrivateCrtKeySpec', 'RSAPrivateKeySpec', 'RSAPublicKeySpec', 'X509EncodedKeySpec'
			),
		44 => array (
			'BatchUpdateException', 'Blob', 'CallableStatement', 'Clob', 'Connection', 'DataTruncation', 'DatabaseMetaData', 'Driver', 'DriverManager', 'DriverPropertyInfo', 'ParameterMetaData', 'PreparedStatement', 'Ref', 'ResultSet', 'ResultSetMetaData', 'SQLData', 'SQLException', 'SQLInput', 'SQLOutput', 'SQLPermission', 'SQLWarning', 'Savepoint', 'Struct', 'Time', 'Types'
			),
		45 => array (
			'AttributedCharacterIterator', 'AttributedCharacterIterator.Attribute', 'AttributedString', 'Bidi', 'BreakIterator', 'CharacterIterator', 'ChoiceFormat', 'CollationElementIterator', 'CollationKey', 'Collator', 'DateFormat', 'DateFormat.Field', 'DateFormatSymbols', 'DecimalFormat', 'DecimalFormatSymbols', 'FieldPosition', 'Format', 'Format.Field', 'MessageFormat', 'MessageFormat.Field', 'NumberFormat', 'NumberFormat.Field', 'ParseException', 'ParsePosition', 'RuleBasedCollator', 'SimpleDateFormat', 'StringCharacterIterator'
			),
		46 => array (
			'AbstractCollection', 'AbstractList', 'AbstractMap', 'AbstractQueue', 'AbstractSequentialList', 'AbstractSet', 'ArrayList', 'Arrays', 'BitSet', 'Calendar', 'Collection', 'Collections', 'Comparator', 'ConcurrentModificationException', 'Currency', 'Dictionary', 'DuplicateFormatFlagsException', 'EmptyStackException', 'EnumMap', 'EnumSet', 'Enumeration', 'EventListenerProxy', 'EventObject', 'FormatFlagsConversionMismatchException', 'Formattable', 'FormattableFlags', 'Formatter.BigDecimalLayoutForm', 'FormatterClosedException', 'GregorianCalendar', 'HashMap', 'HashSet', 'Hashtable', 'IdentityHashMap', 'IllegalFormatCodePointException', 'IllegalFormatConversionException', 'IllegalFormatException', 'IllegalFormatFlagsException', 'IllegalFormatPrecisionException', 'IllegalFormatWidthException', 'InputMismatchException', 'InvalidPropertiesFormatException', 'Iterator', 'LinkedHashMap', 'LinkedHashSet', 'LinkedList', 'ListIterator', 'ListResourceBundle', 'Locale', 'Map', 'Map.Entry', 'MissingFormatArgumentException',
			'MissingFormatWidthException', 'MissingResourceException', 'NoSuchElementException', 'Observable', 'Observer', 'PriorityQueue', 'Properties', 'PropertyPermission', 'PropertyResourceBundle', 'Queue', 'Random', 'RandomAccess', 'ResourceBundle', 'Scanner', 'Set', 'SimpleTimeZone', 'SortedMap', 'SortedSet', 'Stack', 'StringTokenizer', 'TimeZone', 'TimerTask', 'TooManyListenersException', 'TreeMap', 'TreeSet', 'UUID', 'UnknownFormatConversionException', 'UnknownFormatFlagsException', 'Vector', 'WeakHashMap'
			),
		47 => array (
			'AbstractExecutorService', 'ArrayBlockingQueue', 'BlockingQueue', 'BrokenBarrierException', 'Callable', 'CancellationException', 'CompletionService', 'ConcurrentHashMap', 'ConcurrentLinkedQueue', 'ConcurrentMap', 'CopyOnWriteArrayList', 'CopyOnWriteArraySet', 'CountDownLatch', 'CyclicBarrier', 'DelayQueue', 'Delayed', 'Exchanger', 'ExecutionException', 'Executor', 'ExecutorCompletionService', 'ExecutorService', 'Executors', 'Future', 'FutureTask', 'LinkedBlockingQueue', 'PriorityBlockingQueue', 'RejectedExecutionException', 'RejectedExecutionHandler', 'ScheduledExecutorService', 'ScheduledFuture', 'ScheduledThreadPoolExecutor', 'Semaphore', 'SynchronousQueue', 'ThreadFactory', 'ThreadPoolExecutor', 'ThreadPoolExecutor.AbortPolicy', 'ThreadPoolExecutor.CallerRunsPolicy', 'ThreadPoolExecutor.DiscardOldestPolicy', 'ThreadPoolExecutor.DiscardPolicy', 'TimeUnit', 'TimeoutException'
			),
		48 => array (
			'AtomicBoolean', 'AtomicInteger', 'AtomicIntegerArray', 'AtomicIntegerFieldUpdater', 'AtomicLong', 'AtomicLongArray', 'AtomicLongFieldUpdater', 'AtomicMarkableReference', 'AtomicReference', 'AtomicReferenceArray', 'AtomicReferenceFieldUpdater', 'AtomicStampedReference'
			),
		49 => array (
			'AbstractQueuedSynchronizer', 'Condition', 'Lock', 'LockSupport', 'ReadWriteLock', 'ReentrantLock', 'ReentrantReadWriteLock', 'ReentrantReadWriteLock.ReadLock', 'ReentrantReadWriteLock.WriteLock'
			),
		50 => array (
			'Attributes.Name', 'JarEntry', 'JarException', 'JarFile', 'JarInputStream', 'JarOutputStream', 'Manifest', 'Pack200', 'Pack200.Packer', 'Pack200.Unpacker'
			),
		51 => array (
			'ConsoleHandler', 'ErrorManager', 'FileHandler', 'Filter', 'Handler', 'Level', 'LogManager', 'LogRecord', 'Logger', 'LoggingMXBean', 'LoggingPermission', 'MemoryHandler', 'SimpleFormatter', 'SocketHandler', 'StreamHandler', 'XMLFormatter'
			),
		52 => array (
			'AbstractPreferences', 'BackingStoreException', 'InvalidPreferencesFormatException', 'NodeChangeEvent', 'NodeChangeListener', 'PreferenceChangeEvent', 'PreferenceChangeListener', 'Preferences', 'PreferencesFactory'
			),
		53 => array (
			'MatchResult', 'Matcher', 'Pattern', 'PatternSyntaxException'
			),
		54 => array (
			'Adler32', 'CRC32', 'CheckedInputStream', 'CheckedOutputStream', 'Checksum', 'DataFormatException', 'Deflater', 'DeflaterOutputStream', 'GZIPInputStream', 'GZIPOutputStream', 'Inflater', 'InflaterInputStream', 'ZipEntry', 'ZipException', 'ZipFile', 'ZipInputStream', 'ZipOutputStream'
			),
		55 => array (
			'Accessible', 'AccessibleAction', 'AccessibleAttributeSequence', 'AccessibleBundle', 'AccessibleComponent', 'AccessibleContext', 'AccessibleEditableText', 'AccessibleExtendedComponent', 'AccessibleExtendedTable', 'AccessibleExtendedText', 'AccessibleHyperlink', 'AccessibleHypertext', 'AccessibleIcon', 'AccessibleKeyBinding', 'AccessibleRelation', 'AccessibleRelationSet', 'AccessibleResourceBundle', 'AccessibleRole', 'AccessibleSelection', 'AccessibleState', 'AccessibleStateSet', 'AccessibleStreamable', 'AccessibleTable', 'AccessibleTableModelChange', 'AccessibleText', 'AccessibleTextSequence', 'AccessibleValue'
			),
		56 => array (
			'ActivityCompletedException', 'ActivityRequiredException', 'InvalidActivityException'
			),
		57 => array (
			'BadPaddingException', 'Cipher', 'CipherInputStream', 'CipherOutputStream', 'CipherSpi', 'EncryptedPrivateKeyInfo', 'ExemptionMechanism', 'ExemptionMechanismException', 'ExemptionMechanismSpi', 'IllegalBlockSizeException', 'KeyAgreement', 'KeyAgreementSpi', 'KeyGenerator', 'KeyGeneratorSpi', 'Mac', 'MacSpi', 'NoSuchPaddingException', 'NullCipher', 'SealedObject', 'SecretKey', 'SecretKeyFactory', 'SecretKeyFactorySpi', 'ShortBufferException'
			),
		58 => array (
			'DHKey', 'DHPrivateKey', 'DHPublicKey', 'PBEKey'
			),
		59 => array (
			'DESKeySpec', 'DESedeKeySpec', 'DHGenParameterSpec', 'DHParameterSpec', 'DHPrivateKeySpec', 'DHPublicKeySpec', 'IvParameterSpec', 'OAEPParameterSpec', 'PBEKeySpec', 'PBEParameterSpec', 'PSource', 'PSource.PSpecified', 'RC2ParameterSpec', 'RC5ParameterSpec', 'SecretKeySpec'
			),
		60 => array (
			'IIOException', 'IIOImage', 'IIOParam', 'IIOParamController', 'ImageIO', 'ImageReadParam', 'ImageReader', 'ImageTranscoder', 'ImageTypeSpecifier', 'ImageWriteParam', 'ImageWriter'
			),
		61 => array (
			'IIOReadProgressListener', 'IIOReadUpdateListener', 'IIOReadWarningListener', 'IIOWriteProgressListener', 'IIOWriteWarningListener'
			),
		62 => array (
			'IIOInvalidTreeException', 'IIOMetadata', 'IIOMetadataController', 'IIOMetadataFormat', 'IIOMetadataFormatImpl', 'IIOMetadataNode'
			),
		63 => array (
			'BMPImageWriteParam'
			),
		64 => array (
			'JPEGHuffmanTable', 'JPEGImageReadParam', 'JPEGImageWriteParam', 'JPEGQTable'
			),
		65 => array (
			'IIORegistry', 'IIOServiceProvider', 'ImageInputStreamSpi', 'ImageOutputStreamSpi', 'ImageReaderSpi', 'ImageReaderWriterSpi', 'ImageTranscoderSpi', 'ImageWriterSpi', 'RegisterableService', 'ServiceRegistry', 'ServiceRegistry.Filter'
			),
		66 => array (
			'FileCacheImageInputStream', 'FileCacheImageOutputStream', 'FileImageInputStream', 'FileImageOutputStream', 'IIOByteBuffer', 'ImageInputStream', 'ImageInputStreamImpl', 'ImageOutputStream', 'ImageOutputStreamImpl', 'MemoryCacheImageInputStream', 'MemoryCacheImageOutputStream'
			),
		67 => array (
			'AttributeChangeNotification', 'AttributeChangeNotificationFilter', 'AttributeNotFoundException', 'AttributeValueExp', 'BadAttributeValueExpException', 'BadBinaryOpValueExpException', 'BadStringOperationException', 'Descriptor', 'DescriptorAccess', 'DynamicMBean', 'InstanceAlreadyExistsException', 'InstanceNotFoundException', 'InvalidApplicationException', 'JMException', 'JMRuntimeException', 'ListenerNotFoundException', 'MBeanAttributeInfo', 'MBeanConstructorInfo', 'MBeanException', 'MBeanFeatureInfo', 'MBeanInfo', 'MBeanNotificationInfo', 'MBeanOperationInfo', 'MBeanParameterInfo', 'MBeanPermission', 'MBeanRegistration', 'MBeanRegistrationException', 'MBeanServer', 'MBeanServerBuilder', 'MBeanServerConnection', 'MBeanServerDelegate', 'MBeanServerDelegateMBean', 'MBeanServerFactory', 'MBeanServerInvocationHandler', 'MBeanServerNotification', 'MBeanServerPermission', 'MBeanTrustPermission', 'MalformedObjectNameException', 'NotCompliantMBeanException', 'Notification', 'NotificationBroadcaster',
			'NotificationBroadcasterSupport', 'NotificationEmitter', 'NotificationFilter', 'NotificationFilterSupport', 'NotificationListener', 'ObjectInstance', 'ObjectName', 'OperationsException', 'PersistentMBean', 'Query', 'QueryEval', 'QueryExp', 'ReflectionException', 'RuntimeErrorException', 'RuntimeMBeanException', 'RuntimeOperationsException', 'ServiceNotFoundException', 'StandardMBean', 'StringValueExp', 'ValueExp'
			),
		68 => array (
			'ClassLoaderRepository', 'MLet', 'MLetMBean', 'PrivateClassLoader', 'PrivateMLet'
			),
		69 => array (
			'DescriptorSupport', 'InvalidTargetObjectTypeException', 'ModelMBean', 'ModelMBeanAttributeInfo', 'ModelMBeanConstructorInfo', 'ModelMBeanInfo', 'ModelMBeanInfoSupport', 'ModelMBeanNotificationBroadcaster', 'ModelMBeanNotificationInfo', 'ModelMBeanOperationInfo', 'RequiredModelMBean', 'XMLParseException'
			),
		70 => array (
			'CounterMonitor', 'CounterMonitorMBean', 'GaugeMonitor', 'GaugeMonitorMBean', 'Monitor', 'MonitorMBean', 'MonitorNotification', 'MonitorSettingException', 'StringMonitor', 'StringMonitorMBean'
			),
		71 => array (
			'ArrayType', 'CompositeData', 'CompositeDataSupport', 'CompositeType', 'InvalidOpenTypeException', 'KeyAlreadyExistsException', 'OpenDataException', 'OpenMBeanAttributeInfo', 'OpenMBeanAttributeInfoSupport', 'OpenMBeanConstructorInfo', 'OpenMBeanConstructorInfoSupport', 'OpenMBeanInfo', 'OpenMBeanInfoSupport', 'OpenMBeanOperationInfo', 'OpenMBeanOperationInfoSupport', 'OpenMBeanParameterInfo', 'OpenMBeanParameterInfoSupport', 'SimpleType', 'TabularData', 'TabularDataSupport', 'TabularType'
			),
		72 => array (
			'InvalidRelationIdException', 'InvalidRelationServiceException', 'InvalidRelationTypeException', 'InvalidRoleInfoException', 'InvalidRoleValueException', 'MBeanServerNotificationFilter', 'Relation', 'RelationException', 'RelationNotFoundException', 'RelationNotification', 'RelationService', 'RelationServiceMBean', 'RelationServiceNotRegisteredException', 'RelationSupport', 'RelationSupportMBean', 'RelationType', 'RelationTypeNotFoundException', 'RelationTypeSupport', 'Role', 'RoleInfo', 'RoleInfoNotFoundException', 'RoleList', 'RoleNotFoundException', 'RoleResult', 'RoleStatus', 'RoleUnresolved', 'RoleUnresolvedList'
			),
		73 => array (
			'JMXAuthenticator', 'JMXConnectionNotification', 'JMXConnector', 'JMXConnectorFactory', 'JMXConnectorProvider', 'JMXConnectorServer', 'JMXConnectorServerFactory', 'JMXConnectorServerMBean', 'JMXConnectorServerProvider', 'JMXPrincipal', 'JMXProviderException', 'JMXServerErrorException', 'JMXServiceURL', 'MBeanServerForwarder', 'NotificationResult', 'SubjectDelegationPermission', 'TargetedNotification'
			),
		74 => array (
			'RMIConnection', 'RMIConnectionImpl', 'RMIConnectionImpl_Stub', 'RMIConnector', 'RMIConnectorServer', 'RMIIIOPServerImpl', 'RMIJRMPServerImpl', 'RMIServer', 'RMIServerImpl', 'RMIServerImpl_Stub'
			),
		75 => array (
			'TimerAlarmClockNotification', 'TimerMBean', 'TimerNotification'
			),
		76 => array (
			'AuthenticationNotSupportedException', 'BinaryRefAddr', 'CannotProceedException', 'CommunicationException', 'CompositeName', 'CompoundName', 'ConfigurationException', 'ContextNotEmptyException', 'InitialContext', 'InsufficientResourcesException', 'InterruptedNamingException', 'InvalidNameException', 'LimitExceededException', 'LinkException', 'LinkLoopException', 'LinkRef', 'MalformedLinkException', 'Name', 'NameAlreadyBoundException', 'NameClassPair', 'NameNotFoundException', 'NameParser', 'NamingEnumeration', 'NamingException', 'NamingSecurityException', 'NoInitialContextException', 'NoPermissionException', 'NotContextException', 'OperationNotSupportedException', 'PartialResultException', 'RefAddr', 'Referenceable', 'ReferralException', 'ServiceUnavailableException', 'SizeLimitExceededException', 'StringRefAddr', 'TimeLimitExceededException'
			),
		77 => array (
			'AttributeInUseException', 'AttributeModificationException', 'BasicAttribute', 'BasicAttributes', 'DirContext', 'InitialDirContext', 'InvalidAttributeIdentifierException', 'InvalidAttributesException', 'InvalidSearchControlsException', 'InvalidSearchFilterException', 'ModificationItem', 'NoSuchAttributeException', 'SchemaViolationException', 'SearchControls', 'SearchResult'
			),
		78 => array (
			'EventContext', 'EventDirContext', 'NamespaceChangeListener', 'NamingEvent', 'NamingExceptionEvent', 'NamingListener', 'ObjectChangeListener'
			),
		79 => array (
			'BasicControl', 'ControlFactory', 'ExtendedRequest', 'ExtendedResponse', 'HasControls', 'InitialLdapContext', 'LdapContext', 'LdapName', 'LdapReferralException', 'ManageReferralControl', 'PagedResultsControl', 'PagedResultsResponseControl', 'Rdn', 'SortControl', 'SortKey', 'SortResponseControl', 'StartTlsRequest', 'StartTlsResponse', 'UnsolicitedNotification', 'UnsolicitedNotificationEvent', 'UnsolicitedNotificationListener'
			),
		80 => array (
			'DirObjectFactory', 'DirStateFactory', 'DirStateFactory.Result', 'DirectoryManager', 'InitialContextFactory', 'InitialContextFactoryBuilder', 'NamingManager', 'ObjectFactory', 'ObjectFactoryBuilder', 'ResolveResult', 'Resolver', 'StateFactory'
			),
		81 => array (
			'ServerSocketFactory', 'SocketFactory'
			),
		82 => array (
			'CertPathTrustManagerParameters', 'HandshakeCompletedEvent', 'HandshakeCompletedListener', 'HostnameVerifier', 'HttpsURLConnection', 'KeyManager', 'KeyManagerFactory', 'KeyManagerFactorySpi', 'KeyStoreBuilderParameters', 'ManagerFactoryParameters', 'SSLContext', 'SSLContextSpi', 'SSLEngine', 'SSLEngineResult', 'SSLEngineResult.HandshakeStatus', 'SSLEngineResult.Status', 'SSLException', 'SSLHandshakeException', 'SSLKeyException', 'SSLPeerUnverifiedException', 'SSLPermission', 'SSLProtocolException', 'SSLServerSocket', 'SSLServerSocketFactory', 'SSLSession', 'SSLSessionBindingEvent', 'SSLSessionBindingListener', 'SSLSessionContext', 'SSLSocket', 'SSLSocketFactory', 'TrustManager', 'TrustManagerFactory', 'TrustManagerFactorySpi', 'X509ExtendedKeyManager', 'X509KeyManager', 'X509TrustManager'
			),
		83 => array (
			'AttributeException', 'CancelablePrintJob', 'Doc', 'DocFlavor', 'DocFlavor.BYTE_ARRAY', 'DocFlavor.CHAR_ARRAY', 'DocFlavor.INPUT_STREAM', 'DocFlavor.READER', 'DocFlavor.SERVICE_FORMATTED', 'DocFlavor.STRING', 'DocFlavor.URL', 'DocPrintJob', 'FlavorException', 'MultiDoc', 'MultiDocPrintJob', 'MultiDocPrintService', 'PrintException', 'PrintService', 'PrintServiceLookup', 'ServiceUI', 'ServiceUIFactory', 'SimpleDoc', 'StreamPrintService', 'StreamPrintServiceFactory', 'URIException'
			),
		84 => array (
			'AttributeSetUtilities', 'DateTimeSyntax', 'DocAttribute', 'DocAttributeSet', 'EnumSyntax', 'HashAttributeSet', 'HashDocAttributeSet', 'HashPrintJobAttributeSet', 'HashPrintRequestAttributeSet', 'HashPrintServiceAttributeSet', 'IntegerSyntax', 'PrintJobAttribute', 'PrintJobAttributeSet', 'PrintRequestAttribute', 'PrintRequestAttributeSet', 'PrintServiceAttribute', 'PrintServiceAttributeSet', 'ResolutionSyntax', 'SetOfIntegerSyntax', 'Size2DSyntax', 'SupportedValuesAttribute', 'TextSyntax', 'URISyntax', 'UnmodifiableSetException'
			),
		85 => array (
			'Chromaticity', 'ColorSupported', 'Compression', 'Copies', 'CopiesSupported', 'DateTimeAtCompleted', 'DateTimeAtCreation', 'DateTimeAtProcessing', 'Destination', 'DocumentName', 'Fidelity', 'Finishings', 'JobHoldUntil', 'JobImpressions', 'JobImpressionsCompleted', 'JobImpressionsSupported', 'JobKOctets', 'JobKOctetsProcessed', 'JobKOctetsSupported', 'JobMediaSheets', 'JobMediaSheetsCompleted', 'JobMediaSheetsSupported', 'JobMessageFromOperator', 'JobName', 'JobOriginatingUserName', 'JobPriority', 'JobPrioritySupported', 'JobSheets', 'JobState', 'JobStateReason', 'JobStateReasons', 'Media', 'MediaName', 'MediaPrintableArea', 'MediaSize', 'MediaSize.Engineering', 'MediaSize.ISO', 'MediaSize.JIS', 'MediaSize.NA', 'MediaSize.Other', 'MediaSizeName', 'MediaTray', 'MultipleDocumentHandling', 'NumberOfDocuments', 'NumberOfInterveningJobs', 'NumberUp', 'NumberUpSupported', 'OrientationRequested', 'OutputDeviceAssigned', 'PDLOverrideSupported', 'PageRanges', 'PagesPerMinute', 'PagesPerMinuteColor',
			'PresentationDirection', 'PrintQuality', 'PrinterInfo', 'PrinterIsAcceptingJobs', 'PrinterLocation', 'PrinterMakeAndModel', 'PrinterMessageFromOperator', 'PrinterMoreInfo', 'PrinterMoreInfoManufacturer', 'PrinterName', 'PrinterResolution', 'PrinterState', 'PrinterStateReason', 'PrinterStateReasons', 'PrinterURI', 'QueuedJobCount', 'ReferenceUriSchemesSupported', 'RequestingUserName', 'Severity', 'SheetCollate', 'Sides'
			),
		86 => array (
			'PrintEvent', 'PrintJobAdapter', 'PrintJobAttributeEvent', 'PrintJobAttributeListener', 'PrintJobEvent', 'PrintJobListener', 'PrintServiceAttributeEvent', 'PrintServiceAttributeListener'
			),
		87 => array (
			'PortableRemoteObject'
			),
		88 => array (
			'ClassDesc', 'PortableRemoteObjectDelegate', 'Stub', 'StubDelegate', 'Tie', 'Util', 'UtilDelegate', 'ValueHandler', 'ValueHandlerMultiFormat'
			),
		89 => array (
			'SslRMIClientSocketFactory', 'SslRMIServerSocketFactory'
			),
		90 => array (
			'AuthPermission', 'DestroyFailedException', 'Destroyable', 'PrivateCredentialPermission', 'RefreshFailedException', 'Refreshable', 'Subject', 'SubjectDomainCombiner'
			),
		91 => array (
			'Callback', 'CallbackHandler', 'ChoiceCallback', 'ConfirmationCallback', 'LanguageCallback', 'NameCallback', 'PasswordCallback', 'TextInputCallback', 'TextOutputCallback', 'UnsupportedCallbackException'
			),
		92 => array (
			'DelegationPermission', 'KerberosKey', 'KerberosPrincipal', 'KerberosTicket', 'ServicePermission'
			),
		93 => array (
			'AccountException', 'AccountExpiredException', 'AccountLockedException', 'AccountNotFoundException', 'AppConfigurationEntry', 'AppConfigurationEntry.LoginModuleControlFlag', 'Configuration', 'CredentialException', 'CredentialExpiredException', 'CredentialNotFoundException', 'FailedLoginException', 'LoginContext', 'LoginException'
			),
		94 => array (
			'LoginModule'
			),
		95 => array (
			'X500Principal', 'X500PrivateCredential'
			),
		96 => array (
			'AuthorizeCallback', 'RealmCallback', 'RealmChoiceCallback', 'Sasl', 'SaslClient', 'SaslClientFactory', 'SaslException', 'SaslServer', 'SaslServerFactory'
			),
		97 => array (
			'ControllerEventListener', 'Instrument', 'InvalidMidiDataException', 'MetaEventListener', 'MetaMessage', 'MidiChannel', 'MidiDevice', 'MidiDevice.Info', 'MidiEvent', 'MidiFileFormat', 'MidiMessage', 'MidiSystem', 'MidiUnavailableException', 'Patch', 'Receiver', 'Sequence', 'Sequencer', 'Sequencer.SyncMode', 'ShortMessage', 'Soundbank', 'SoundbankResource', 'Synthesizer', 'SysexMessage', 'Track', 'Transmitter', 'VoiceStatus'
			),
		98 => array (
			'MidiDeviceProvider', 'MidiFileReader', 'MidiFileWriter', 'SoundbankReader'
			),
		99 => array (
			'AudioFileFormat', 'AudioFileFormat.Type', 'AudioFormat', 'AudioFormat.Encoding', 'AudioInputStream', 'AudioPermission', 'AudioSystem', 'BooleanControl', 'BooleanControl.Type', 'Clip', 'CompoundControl', 'CompoundControl.Type', 'Control.Type', 'DataLine', 'DataLine.Info', 'EnumControl', 'EnumControl.Type', 'FloatControl', 'FloatControl.Type', 'Line', 'Line.Info', 'LineEvent', 'LineEvent.Type', 'LineListener', 'LineUnavailableException', 'Mixer', 'Mixer.Info', 'Port', 'Port.Info', 'ReverbType', 'SourceDataLine', 'TargetDataLine', 'UnsupportedAudioFileException'
			),
		100 => array (
			'AudioFileReader', 'AudioFileWriter', 'FormatConversionProvider', 'MixerProvider'
			),
		101 => array (
			'ConnectionEvent', 'ConnectionEventListener', 'ConnectionPoolDataSource', 'DataSource', 'PooledConnection', 'RowSet', 'RowSetEvent', 'RowSetInternal', 'RowSetListener', 'RowSetMetaData', 'RowSetReader', 'RowSetWriter', 'XAConnection', 'XADataSource'
			),
		102 => array (
			'BaseRowSet', 'CachedRowSet', 'FilteredRowSet', 'JdbcRowSet', 'JoinRowSet', 'Joinable', 'Predicate', 'RowSetMetaDataImpl', 'RowSetWarning', 'WebRowSet'
			),
		103 => array (
			'SQLInputImpl', 'SQLOutputImpl', 'SerialArray', 'SerialBlob', 'SerialClob', 'SerialDatalink', 'SerialException', 'SerialJavaObject', 'SerialRef', 'SerialStruct'
			),
		104 => array (
			'SyncFactory', 'SyncFactoryException', 'SyncProvider', 'SyncProviderException', 'SyncResolver', 'TransactionalWriter', 'XmlReader', 'XmlWriter'
			),
		105 => array (
			'AbstractAction', 'AbstractButton', 'AbstractCellEditor', 'AbstractListModel', 'AbstractSpinnerModel', 'Action', 'ActionMap', 'BorderFactory', 'BoundedRangeModel', 'Box', 'Box.Filler', 'BoxLayout', 'ButtonGroup', 'ButtonModel', 'CellEditor', 'CellRendererPane', 'ComboBoxEditor', 'ComboBoxModel', 'ComponentInputMap', 'DebugGraphics', 'DefaultBoundedRangeModel', 'DefaultButtonModel', 'DefaultCellEditor', 'DefaultComboBoxModel', 'DefaultDesktopManager', 'DefaultFocusManager', 'DefaultListCellRenderer', 'DefaultListCellRenderer.UIResource', 'DefaultListModel', 'DefaultListSelectionModel', 'DefaultSingleSelectionModel', 'DesktopManager', 'FocusManager', 'GrayFilter', 'Icon', 'ImageIcon', 'InputMap', 'InputVerifier', 'InternalFrameFocusTraversalPolicy', 'JApplet', 'JButton', 'JCheckBox', 'JCheckBoxMenuItem', 'JColorChooser', 'JComboBox', 'JComboBox.KeySelectionManager', 'JComponent', 'JDesktopPane', 'JDialog', 'JEditorPane', 'JFileChooser', 'JFormattedTextField', 'JFormattedTextField.AbstractFormatter',
			'JFormattedTextField.AbstractFormatterFactory', 'JFrame', 'JInternalFrame', 'JInternalFrame.JDesktopIcon', 'JLabel', 'JLayeredPane', 'JList', 'JMenu', 'JMenuBar', 'JMenuItem', 'JOptionPane', 'JPanel', 'JPasswordField', 'JPopupMenu', 'JPopupMenu.Separator', 'JProgressBar', 'JRadioButton', 'JRadioButtonMenuItem', 'JRootPane', 'JScrollBar', 'JScrollPane', 'JSeparator', 'JSlider', 'JSpinner', 'JSpinner.DateEditor', 'JSpinner.DefaultEditor', 'JSpinner.ListEditor', 'JSpinner.NumberEditor', 'JSplitPane', 'JTabbedPane', 'JTable', 'JTable.PrintMode', 'JTextArea', 'JTextField', 'JTextPane', 'JToggleButton', 'JToggleButton.ToggleButtonModel', 'JToolBar', 'JToolBar.Separator', 'JToolTip', 'JTree', 'JTree.DynamicUtilTreeNode', 'JTree.EmptySelectionModel', 'JViewport', 'JWindow', 'KeyStroke', 'LayoutFocusTraversalPolicy', 'ListCellRenderer', 'ListModel', 'ListSelectionModel', 'LookAndFeel', 'MenuElement', 'MenuSelectionManager', 'MutableComboBoxModel', 'OverlayLayout', 'Popup', 'PopupFactory', 'ProgressMonitor',
			'ProgressMonitorInputStream', 'Renderer', 'RepaintManager', 'RootPaneContainer', 'ScrollPaneConstants', 'ScrollPaneLayout', 'ScrollPaneLayout.UIResource', 'Scrollable', 'SingleSelectionModel', 'SizeRequirements', 'SizeSequence', 'SortingFocusTraversalPolicy', 'SpinnerDateModel', 'SpinnerListModel', 'SpinnerModel', 'SpinnerNumberModel', 'Spring', 'SpringLayout', 'SpringLayout.Constraints', 'SwingConstants', 'SwingUtilities', 'ToolTipManager', 'TransferHandler', 'UIDefaults', 'UIDefaults.ActiveValue', 'UIDefaults.LazyInputMap', 'UIDefaults.LazyValue', 'UIDefaults.ProxyLazyValue', 'UIManager', 'UIManager.LookAndFeelInfo', 'UnsupportedLookAndFeelException', 'ViewportLayout', 'WindowConstants'
			),
		106 => array (
			'AbstractBorder', 'BevelBorder', 'Border', 'CompoundBorder', 'EmptyBorder', 'EtchedBorder', 'LineBorder', 'MatteBorder', 'SoftBevelBorder', 'TitledBorder'
			),
		107 => array (
			'AbstractColorChooserPanel', 'ColorChooserComponentFactory', 'ColorSelectionModel', 'DefaultColorSelectionModel'
			),
		108 => array (
			'AncestorEvent', 'AncestorListener', 'CaretEvent', 'CaretListener', 'CellEditorListener', 'ChangeEvent', 'ChangeListener', 'DocumentEvent.ElementChange', 'DocumentEvent.EventType', 'DocumentListener', 'EventListenerList', 'HyperlinkEvent', 'HyperlinkEvent.EventType', 'HyperlinkListener', 'InternalFrameAdapter', 'InternalFrameEvent', 'InternalFrameListener', 'ListDataEvent', 'ListDataListener', 'ListSelectionEvent', 'ListSelectionListener', 'MenuDragMouseEvent', 'MenuDragMouseListener', 'MenuEvent', 'MenuKeyEvent', 'MenuKeyListener', 'MenuListener', 'MouseInputAdapter', 'MouseInputListener', 'PopupMenuEvent', 'PopupMenuListener', 'SwingPropertyChangeSupport', 'TableColumnModelEvent', 'TableColumnModelListener', 'TableModelEvent', 'TableModelListener', 'TreeExpansionEvent', 'TreeExpansionListener', 'TreeModelEvent', 'TreeModelListener', 'TreeSelectionEvent', 'TreeSelectionListener', 'TreeWillExpandListener', 'UndoableEditEvent', 'UndoableEditListener'
			),
		109 => array (
			'FileSystemView', 'FileView'
			),
		110 => array (
			'ActionMapUIResource', 'BorderUIResource', 'BorderUIResource.BevelBorderUIResource', 'BorderUIResource.CompoundBorderUIResource', 'BorderUIResource.EmptyBorderUIResource', 'BorderUIResource.EtchedBorderUIResource', 'BorderUIResource.LineBorderUIResource', 'BorderUIResource.MatteBorderUIResource', 'BorderUIResource.TitledBorderUIResource', 'ButtonUI', 'ColorChooserUI', 'ColorUIResource', 'ComboBoxUI', 'ComponentInputMapUIResource', 'ComponentUI', 'DesktopIconUI', 'DesktopPaneUI', 'DimensionUIResource', 'FileChooserUI', 'FontUIResource', 'IconUIResource', 'InputMapUIResource', 'InsetsUIResource', 'InternalFrameUI', 'LabelUI', 'ListUI', 'MenuBarUI', 'MenuItemUI', 'OptionPaneUI', 'PanelUI', 'PopupMenuUI', 'ProgressBarUI', 'RootPaneUI', 'ScrollBarUI', 'ScrollPaneUI', 'SeparatorUI', 'SliderUI', 'SpinnerUI', 'SplitPaneUI', 'TabbedPaneUI', 'TableHeaderUI', 'TableUI', 'TextUI', 'ToolBarUI', 'ToolTipUI', 'TreeUI', 'UIResource', 'ViewportUI'
			),
		111 => array (
			'BasicArrowButton', 'BasicBorders', 'BasicBorders.ButtonBorder', 'BasicBorders.FieldBorder', 'BasicBorders.MarginBorder', 'BasicBorders.MenuBarBorder', 'BasicBorders.RadioButtonBorder', 'BasicBorders.RolloverButtonBorder', 'BasicBorders.SplitPaneBorder', 'BasicBorders.ToggleButtonBorder', 'BasicButtonListener', 'BasicButtonUI', 'BasicCheckBoxMenuItemUI', 'BasicCheckBoxUI', 'BasicColorChooserUI', 'BasicComboBoxEditor', 'BasicComboBoxEditor.UIResource', 'BasicComboBoxRenderer', 'BasicComboBoxRenderer.UIResource', 'BasicComboBoxUI', 'BasicComboPopup', 'BasicDesktopIconUI', 'BasicDesktopPaneUI', 'BasicDirectoryModel', 'BasicEditorPaneUI', 'BasicFileChooserUI', 'BasicFormattedTextFieldUI', 'BasicGraphicsUtils', 'BasicHTML', 'BasicIconFactory', 'BasicInternalFrameTitlePane', 'BasicInternalFrameUI', 'BasicLabelUI', 'BasicListUI', 'BasicLookAndFeel', 'BasicMenuBarUI', 'BasicMenuItemUI', 'BasicMenuUI', 'BasicOptionPaneUI', 'BasicOptionPaneUI.ButtonAreaLayout', 'BasicPanelUI', 'BasicPasswordFieldUI',
			'BasicPopupMenuSeparatorUI', 'BasicPopupMenuUI', 'BasicProgressBarUI', 'BasicRadioButtonMenuItemUI', 'BasicRadioButtonUI', 'BasicRootPaneUI', 'BasicScrollBarUI', 'BasicScrollPaneUI', 'BasicSeparatorUI', 'BasicSliderUI', 'BasicSpinnerUI', 'BasicSplitPaneDivider', 'BasicSplitPaneUI', 'BasicTabbedPaneUI', 'BasicTableHeaderUI', 'BasicTableUI', 'BasicTextAreaUI', 'BasicTextFieldUI', 'BasicTextPaneUI', 'BasicTextUI', 'BasicTextUI.BasicCaret', 'BasicTextUI.BasicHighlighter', 'BasicToggleButtonUI', 'BasicToolBarSeparatorUI', 'BasicToolBarUI', 'BasicToolTipUI', 'BasicTreeUI', 'BasicViewportUI', 'ComboPopup', 'DefaultMenuLayout'
			),
		112 => array (
			'DefaultMetalTheme', 'MetalBorders', 'MetalBorders.ButtonBorder', 'MetalBorders.Flush3DBorder', 'MetalBorders.InternalFrameBorder', 'MetalBorders.MenuBarBorder', 'MetalBorders.MenuItemBorder', 'MetalBorders.OptionDialogBorder', 'MetalBorders.PaletteBorder', 'MetalBorders.PopupMenuBorder', 'MetalBorders.RolloverButtonBorder', 'MetalBorders.ScrollPaneBorder', 'MetalBorders.TableHeaderBorder', 'MetalBorders.TextFieldBorder', 'MetalBorders.ToggleButtonBorder', 'MetalBorders.ToolBarBorder', 'MetalButtonUI', 'MetalCheckBoxIcon', 'MetalCheckBoxUI', 'MetalComboBoxButton', 'MetalComboBoxEditor', 'MetalComboBoxEditor.UIResource', 'MetalComboBoxIcon', 'MetalComboBoxUI', 'MetalDesktopIconUI', 'MetalFileChooserUI', 'MetalIconFactory', 'MetalIconFactory.FileIcon16', 'MetalIconFactory.FolderIcon16', 'MetalIconFactory.PaletteCloseIcon', 'MetalIconFactory.TreeControlIcon', 'MetalIconFactory.TreeFolderIcon', 'MetalIconFactory.TreeLeafIcon', 'MetalInternalFrameTitlePane', 'MetalInternalFrameUI', 'MetalLabelUI',
			'MetalLookAndFeel', 'MetalMenuBarUI', 'MetalPopupMenuSeparatorUI', 'MetalProgressBarUI', 'MetalRadioButtonUI', 'MetalRootPaneUI', 'MetalScrollBarUI', 'MetalScrollButton', 'MetalScrollPaneUI', 'MetalSeparatorUI', 'MetalSliderUI', 'MetalSplitPaneUI', 'MetalTabbedPaneUI', 'MetalTextFieldUI', 'MetalTheme', 'MetalToggleButtonUI', 'MetalToolBarUI', 'MetalToolTipUI', 'MetalTreeUI', 'OceanTheme'
			),
		113 => array (
			'MultiButtonUI', 'MultiColorChooserUI', 'MultiComboBoxUI', 'MultiDesktopIconUI', 'MultiDesktopPaneUI', 'MultiFileChooserUI', 'MultiInternalFrameUI', 'MultiLabelUI', 'MultiListUI', 'MultiLookAndFeel', 'MultiMenuBarUI', 'MultiMenuItemUI', 'MultiOptionPaneUI', 'MultiPanelUI', 'MultiPopupMenuUI', 'MultiProgressBarUI', 'MultiRootPaneUI', 'MultiScrollBarUI', 'MultiScrollPaneUI', 'MultiSeparatorUI', 'MultiSliderUI', 'MultiSpinnerUI', 'MultiSplitPaneUI', 'MultiTabbedPaneUI', 'MultiTableHeaderUI', 'MultiTableUI', 'MultiTextUI', 'MultiToolBarUI', 'MultiToolTipUI', 'MultiTreeUI', 'MultiViewportUI'
			),
		114 => array (
			'ColorType', 'Region', 'SynthConstants', 'SynthContext', 'SynthGraphicsUtils', 'SynthLookAndFeel', 'SynthPainter', 'SynthStyle', 'SynthStyleFactory'
			),
		115 => array (
			'AbstractTableModel', 'DefaultTableCellRenderer', 'DefaultTableCellRenderer.UIResource', 'DefaultTableColumnModel', 'DefaultTableModel', 'JTableHeader', 'TableCellEditor', 'TableCellRenderer', 'TableColumn', 'TableColumnModel', 'TableModel'
			),
		116 => array (
			'AbstractDocument', 'AbstractDocument.AttributeContext', 'AbstractDocument.Content', 'AbstractDocument.ElementEdit', 'AbstractWriter', 'AsyncBoxView', 'AttributeSet.CharacterAttribute', 'AttributeSet.ColorAttribute', 'AttributeSet.FontAttribute', 'AttributeSet.ParagraphAttribute', 'BadLocationException', 'BoxView', 'Caret', 'ChangedCharSetException', 'ComponentView', 'CompositeView', 'DateFormatter', 'DefaultCaret', 'DefaultEditorKit', 'DefaultEditorKit.BeepAction', 'DefaultEditorKit.CopyAction', 'DefaultEditorKit.CutAction', 'DefaultEditorKit.DefaultKeyTypedAction', 'DefaultEditorKit.InsertBreakAction', 'DefaultEditorKit.InsertContentAction', 'DefaultEditorKit.InsertTabAction', 'DefaultEditorKit.PasteAction', 'DefaultFormatter', 'DefaultFormatterFactory', 'DefaultHighlighter', 'DefaultHighlighter.DefaultHighlightPainter', 'DefaultStyledDocument', 'DefaultStyledDocument.AttributeUndoableEdit', 'DefaultStyledDocument.ElementSpec', 'DefaultTextUI', 'DocumentFilter', 'DocumentFilter.FilterBypass',
			'EditorKit', 'ElementIterator', 'FieldView', 'FlowView', 'FlowView.FlowStrategy', 'GapContent', 'GlyphView', 'GlyphView.GlyphPainter', 'Highlighter', 'Highlighter.Highlight', 'Highlighter.HighlightPainter', 'IconView', 'InternationalFormatter', 'JTextComponent', 'JTextComponent.KeyBinding', 'Keymap', 'LabelView', 'LayeredHighlighter', 'LayeredHighlighter.LayerPainter', 'LayoutQueue', 'MaskFormatter', 'MutableAttributeSet', 'NavigationFilter', 'NavigationFilter.FilterBypass', 'NumberFormatter', 'PasswordView', 'PlainDocument', 'PlainView', 'Position', 'Position.Bias', 'Segment', 'SimpleAttributeSet', 'StringContent', 'Style', 'StyleConstants', 'StyleConstants.CharacterConstants', 'StyleConstants.ColorConstants', 'StyleConstants.FontConstants', 'StyleConstants.ParagraphConstants', 'StyleContext', 'StyledDocument', 'StyledEditorKit', 'StyledEditorKit.AlignmentAction', 'StyledEditorKit.BoldAction', 'StyledEditorKit.FontFamilyAction', 'StyledEditorKit.FontSizeAction', 'StyledEditorKit.ForegroundAction',
			'StyledEditorKit.ItalicAction', 'StyledEditorKit.StyledTextAction', 'StyledEditorKit.UnderlineAction', 'TabExpander', 'TabSet', 'TabStop', 'TabableView', 'TableView', 'TextAction', 'Utilities', 'View', 'ViewFactory', 'WrappedPlainView', 'ZoneView'
			),
		117 => array (
			'BlockView', 'CSS', 'CSS.Attribute', 'FormSubmitEvent', 'FormSubmitEvent.MethodType', 'FormView', 'HTML', 'HTML.Attribute', 'HTML.Tag', 'HTML.UnknownTag', 'HTMLDocument', 'HTMLDocument.Iterator', 'HTMLEditorKit', 'HTMLEditorKit.HTMLFactory', 'HTMLEditorKit.HTMLTextAction', 'HTMLEditorKit.InsertHTMLTextAction', 'HTMLEditorKit.LinkController', 'HTMLEditorKit.Parser', 'HTMLEditorKit.ParserCallback', 'HTMLFrameHyperlinkEvent', 'HTMLWriter', 'ImageView', 'InlineView', 'ListView', 'MinimalHTMLWriter', 'ObjectView', 'Option', 'StyleSheet', 'StyleSheet.BoxPainter', 'StyleSheet.ListPainter'
			),
		118 => array (
			'ContentModel', 'DTD', 'DTDConstants', 'DocumentParser', 'ParserDelegator', 'TagElement'
			),
		119 => array (
			'RTFEditorKit'
			),
		120 => array (
			'AbstractLayoutCache', 'AbstractLayoutCache.NodeDimensions', 'DefaultMutableTreeNode', 'DefaultTreeCellEditor', 'DefaultTreeCellRenderer', 'DefaultTreeModel', 'DefaultTreeSelectionModel', 'ExpandVetoException', 'FixedHeightLayoutCache', 'MutableTreeNode', 'RowMapper', 'TreeCellEditor', 'TreeCellRenderer', 'TreeModel', 'TreeNode', 'TreePath', 'TreeSelectionModel', 'VariableHeightLayoutCache'
			),
		121 => array (
			'AbstractUndoableEdit', 'CannotRedoException', 'CannotUndoException', 'CompoundEdit', 'StateEdit', 'StateEditable', 'UndoManager', 'UndoableEdit', 'UndoableEditSupport'
			),
		122 => array (
			'InvalidTransactionException', 'TransactionRequiredException', 'TransactionRolledbackException'
			),
		123 => array (
			'XAException', 'XAResource', 'Xid'
			),
		124 => array (
			'XMLConstants'
			),
		125 => array (
			'DatatypeConfigurationException', 'DatatypeConstants', 'DatatypeConstants.Field', 'DatatypeFactory', 'Duration', 'XMLGregorianCalendar'
			),
		126 => array (
			'NamespaceContext', 'QName'
			),
		127 => array (
			'DocumentBuilder', 'DocumentBuilderFactory', 'FactoryConfigurationError', 'ParserConfigurationException', 'SAXParser', 'SAXParserFactory'
			),
		128 => array (
			'ErrorListener', 'OutputKeys', 'Result', 'Source', 'SourceLocator', 'Templates', 'Transformer', 'TransformerConfigurationException', 'TransformerException', 'TransformerFactory', 'TransformerFactoryConfigurationError', 'URIResolver'
			),
		129 => array (
			'DOMResult', 'DOMSource'
			),
		130 => array (
			'SAXResult', 'SAXSource', 'SAXTransformerFactory', 'TemplatesHandler', 'TransformerHandler'
			),
		131 => array (
			'StreamResult', 'StreamSource'
			),
		132 => array (
			'Schema', 'SchemaFactory', 'SchemaFactoryLoader', 'TypeInfoProvider', 'Validator', 'ValidatorHandler'
			),
		133 => array (
			'XPath', 'XPathConstants', 'XPathException', 'XPathExpression', 'XPathExpressionException', 'XPathFactory', 'XPathFactoryConfigurationException', 'XPathFunction', 'XPathFunctionException', 'XPathFunctionResolver', 'XPathVariableResolver'
			),
		134 => array (
			'ChannelBinding', 'GSSContext', 'GSSCredential', 'GSSException', 'GSSManager', 'GSSName', 'MessageProp', 'Oid'
			),
		135 => array (
			'ACTIVITY_COMPLETED', 'ACTIVITY_REQUIRED', 'ARG_IN', 'ARG_INOUT', 'ARG_OUT', 'Any', 'AnyHolder', 'AnySeqHolder', 'BAD_CONTEXT', 'BAD_INV_ORDER', 'BAD_OPERATION', 'BAD_PARAM', 'BAD_POLICY', 'BAD_POLICY_TYPE', 'BAD_POLICY_VALUE', 'BAD_QOS', 'BAD_TYPECODE', 'BooleanHolder', 'BooleanSeqHelper', 'BooleanSeqHolder', 'ByteHolder', 'CODESET_INCOMPATIBLE', 'COMM_FAILURE', 'CTX_RESTRICT_SCOPE', 'CharHolder', 'CharSeqHelper', 'CharSeqHolder', 'CompletionStatus', 'CompletionStatusHelper', 'ContextList', 'CurrentHolder', 'CustomMarshal', 'DATA_CONVERSION', 'DefinitionKind', 'DefinitionKindHelper', 'DomainManager', 'DomainManagerOperations', 'DoubleHolder', 'DoubleSeqHelper', 'DoubleSeqHolder', 'Environment', 'ExceptionList', 'FREE_MEM', 'FixedHolder', 'FloatHolder', 'FloatSeqHelper', 'FloatSeqHolder', 'IDLType', 'IDLTypeHelper', 'IDLTypeOperations', 'IMP_LIMIT', 'INITIALIZE', 'INTERNAL', 'INTF_REPOS', 'INVALID_ACTIVITY', 'INVALID_TRANSACTION', 'INV_FLAG', 'INV_IDENT', 'INV_OBJREF', 'INV_POLICY', 'IRObject',
			'IRObjectOperations', 'IdentifierHelper', 'IntHolder', 'LocalObject', 'LongHolder', 'LongLongSeqHelper', 'LongLongSeqHolder', 'LongSeqHelper', 'LongSeqHolder', 'MARSHAL', 'NO_IMPLEMENT', 'NO_MEMORY', 'NO_PERMISSION', 'NO_RESOURCES', 'NO_RESPONSE', 'NVList', 'NamedValue', 'OBJECT_NOT_EXIST', 'OBJ_ADAPTER', 'OMGVMCID', 'ObjectHelper', 'ObjectHolder', 'OctetSeqHelper', 'OctetSeqHolder', 'PERSIST_STORE', 'PRIVATE_MEMBER', 'PUBLIC_MEMBER', 'ParameterMode', 'ParameterModeHelper', 'ParameterModeHolder', 'PolicyError', 'PolicyErrorCodeHelper', 'PolicyErrorHelper', 'PolicyErrorHolder', 'PolicyHelper', 'PolicyHolder', 'PolicyListHelper', 'PolicyListHolder', 'PolicyOperations', 'PolicyTypeHelper', 'PrincipalHolder', 'REBIND', 'RepositoryIdHelper', 'Request', 'ServerRequest', 'ServiceDetail', 'ServiceDetailHelper', 'ServiceInformation', 'ServiceInformationHelper', 'ServiceInformationHolder', 'SetOverrideType', 'SetOverrideTypeHelper', 'ShortHolder', 'ShortSeqHelper', 'ShortSeqHolder', 'StringHolder',
			'StringSeqHelper', 'StringSeqHolder', 'StringValueHelper', 'StructMember', 'StructMemberHelper', 'SystemException', 'TCKind', 'TIMEOUT', 'TRANSACTION_MODE', 'TRANSACTION_REQUIRED', 'TRANSACTION_ROLLEDBACK', 'TRANSACTION_UNAVAILABLE', 'TRANSIENT', 'TypeCode', 'TypeCodeHolder', 'ULongLongSeqHelper', 'ULongLongSeqHolder', 'ULongSeqHelper', 'ULongSeqHolder', 'UNSUPPORTED_POLICY', 'UNSUPPORTED_POLICY_VALUE', 'UShortSeqHelper', 'UShortSeqHolder', 'UnionMember', 'UnionMemberHelper', 'UnknownUserException', 'UnknownUserExceptionHelper', 'UnknownUserExceptionHolder', 'UserException', 'VM_ABSTRACT', 'VM_CUSTOM', 'VM_NONE', 'VM_TRUNCATABLE', 'ValueBaseHelper', 'ValueBaseHolder', 'ValueMember', 'ValueMemberHelper', 'VersionSpecHelper', 'VisibilityHelper', 'WCharSeqHelper', 'WCharSeqHolder', 'WStringSeqHelper', 'WStringSeqHolder', 'WStringValueHelper', 'WrongTransaction', 'WrongTransactionHelper', 'WrongTransactionHolder', '_IDLTypeStub', '_PolicyStub'
			),
		136 => array (
			'Invalid', 'InvalidSeq'
			),
		137 => array (
			'BadKind'
			),
		138 => array (
			'ApplicationException', 'BoxedValueHelper', 'CustomValue', 'IDLEntity', 'IndirectionException', 'InvokeHandler', 'RemarshalException', 'ResponseHandler', 'ServantObject', 'Streamable', 'StreamableValue', 'UnknownException', 'ValueBase', 'ValueFactory', 'ValueInputStream', 'ValueOutputStream'
			),
		139 => array (
			'BindingHelper', 'BindingHolder', 'BindingIterator', 'BindingIteratorHelper', 'BindingIteratorHolder', 'BindingIteratorOperations', 'BindingIteratorPOA', 'BindingListHelper', 'BindingListHolder', 'BindingType', 'BindingTypeHelper', 'BindingTypeHolder', 'IstringHelper', 'NameComponent', 'NameComponentHelper', 'NameComponentHolder', 'NameHelper', 'NameHolder', 'NamingContext', 'NamingContextExt', 'NamingContextExtHelper', 'NamingContextExtHolder', 'NamingContextExtOperations', 'NamingContextExtPOA', 'NamingContextHelper', 'NamingContextHolder', 'NamingContextOperations', 'NamingContextPOA', '_BindingIteratorImplBase', '_BindingIteratorStub', '_NamingContextExtStub', '_NamingContextImplBase', '_NamingContextStub'
			),
		140 => array (
			'AddressHelper', 'InvalidAddress', 'InvalidAddressHelper', 'InvalidAddressHolder', 'StringNameHelper', 'URLStringHelper'
			),
		141 => array (
			'AlreadyBound', 'AlreadyBoundHelper', 'AlreadyBoundHolder', 'CannotProceed', 'CannotProceedHelper', 'CannotProceedHolder', 'InvalidNameHolder', 'NotEmpty', 'NotEmptyHelper', 'NotEmptyHolder', 'NotFound', 'NotFoundHelper', 'NotFoundHolder', 'NotFoundReason', 'NotFoundReasonHelper', 'NotFoundReasonHolder'
			),
		142 => array (
			'Parameter'
			),
		143 => array (
			'DynAnyFactory', 'DynAnyFactoryHelper', 'DynAnyFactoryOperations', 'DynAnyHelper', 'DynAnyOperations', 'DynAnySeqHelper', 'DynArrayHelper', 'DynArrayOperations', 'DynEnumHelper', 'DynEnumOperations', 'DynFixedHelper', 'DynFixedOperations', 'DynSequenceHelper', 'DynSequenceOperations', 'DynStructHelper', 'DynStructOperations', 'DynUnionHelper', 'DynUnionOperations', 'DynValueBox', 'DynValueBoxOperations', 'DynValueCommon', 'DynValueCommonOperations', 'DynValueHelper', 'DynValueOperations', 'NameDynAnyPair', 'NameDynAnyPairHelper', 'NameDynAnyPairSeqHelper', 'NameValuePairSeqHelper', '_DynAnyFactoryStub', '_DynAnyStub', '_DynArrayStub', '_DynEnumStub', '_DynFixedStub', '_DynSequenceStub', '_DynStructStub', '_DynUnionStub', '_DynValueStub'
			),
		144 => array (
			'InconsistentTypeCodeHelper'
			),
		145 => array (
			'InvalidValueHelper'
			),
		146 => array (
			'CodeSets', 'Codec', 'CodecFactory', 'CodecFactoryHelper', 'CodecFactoryOperations', 'CodecOperations', 'ComponentIdHelper', 'ENCODING_CDR_ENCAPS', 'Encoding', 'ExceptionDetailMessage', 'IOR', 'IORHelper', 'IORHolder', 'MultipleComponentProfileHelper', 'MultipleComponentProfileHolder', 'ProfileIdHelper', 'RMICustomMaxStreamFormat', 'ServiceContext', 'ServiceContextHelper', 'ServiceContextHolder', 'ServiceContextListHelper', 'ServiceContextListHolder', 'ServiceIdHelper', 'TAG_ALTERNATE_IIOP_ADDRESS', 'TAG_CODE_SETS', 'TAG_INTERNET_IOP', 'TAG_JAVA_CODEBASE', 'TAG_MULTIPLE_COMPONENTS', 'TAG_ORB_TYPE', 'TAG_POLICIES', 'TAG_RMI_CUSTOM_MAX_STREAM_FORMAT', 'TaggedComponent', 'TaggedComponentHelper', 'TaggedComponentHolder', 'TaggedProfile', 'TaggedProfileHelper', 'TaggedProfileHolder', 'TransactionService'
			),
		147 => array (
			'UnknownEncoding', 'UnknownEncodingHelper'
			),
		148 => array (
			'FormatMismatch', 'FormatMismatchHelper', 'InvalidTypeForEncoding', 'InvalidTypeForEncodingHelper'
			),
		149 => array (
			'SYNC_WITH_TRANSPORT', 'SyncScopeHelper'
			),
		150 => array (
			'ACTIVE', 'AdapterManagerIdHelper', 'AdapterNameHelper', 'AdapterStateHelper', 'ClientRequestInfo', 'ClientRequestInfoOperations', 'ClientRequestInterceptor', 'ClientRequestInterceptorOperations', 'DISCARDING', 'HOLDING', 'INACTIVE', 'IORInfo', 'IORInfoOperations', 'IORInterceptor', 'IORInterceptorOperations', 'IORInterceptor_3_0', 'IORInterceptor_3_0Helper', 'IORInterceptor_3_0Holder', 'IORInterceptor_3_0Operations', 'Interceptor', 'InterceptorOperations', 'InvalidSlot', 'InvalidSlotHelper', 'LOCATION_FORWARD', 'NON_EXISTENT', 'ORBIdHelper', 'ORBInitInfo', 'ORBInitInfoOperations', 'ORBInitializer', 'ORBInitializerOperations', 'ObjectReferenceFactory', 'ObjectReferenceFactoryHelper', 'ObjectReferenceFactoryHolder', 'ObjectReferenceTemplate', 'ObjectReferenceTemplateHelper', 'ObjectReferenceTemplateHolder', 'ObjectReferenceTemplateSeqHelper', 'ObjectReferenceTemplateSeqHolder', 'PolicyFactory', 'PolicyFactoryOperations', 'RequestInfo', 'RequestInfoOperations', 'SUCCESSFUL', 'SYSTEM_EXCEPTION',
			'ServerIdHelper', 'ServerRequestInfo', 'ServerRequestInfoOperations', 'ServerRequestInterceptor', 'ServerRequestInterceptorOperations', 'TRANSPORT_RETRY', 'USER_EXCEPTION'
			),
		151 => array (
			'DuplicateName', 'DuplicateNameHelper'
			),
		152 => array (
			'AdapterActivator', 'AdapterActivatorOperations', 'ID_ASSIGNMENT_POLICY_ID', 'ID_UNIQUENESS_POLICY_ID', 'IMPLICIT_ACTIVATION_POLICY_ID', 'IdAssignmentPolicy', 'IdAssignmentPolicyOperations', 'IdAssignmentPolicyValue', 'IdUniquenessPolicy', 'IdUniquenessPolicyOperations', 'IdUniquenessPolicyValue', 'ImplicitActivationPolicy', 'ImplicitActivationPolicyOperations', 'ImplicitActivationPolicyValue', 'LIFESPAN_POLICY_ID', 'LifespanPolicy', 'LifespanPolicyOperations', 'LifespanPolicyValue', 'POA', 'POAHelper', 'POAManager', 'POAManagerOperations', 'POAOperations', 'REQUEST_PROCESSING_POLICY_ID', 'RequestProcessingPolicy', 'RequestProcessingPolicyOperations', 'RequestProcessingPolicyValue', 'SERVANT_RETENTION_POLICY_ID', 'Servant', 'ServantActivator', 'ServantActivatorHelper', 'ServantActivatorOperations', 'ServantActivatorPOA', 'ServantLocator', 'ServantLocatorHelper', 'ServantLocatorOperations', 'ServantLocatorPOA', 'ServantManager', 'ServantManagerOperations', 'ServantRetentionPolicy',
			'ServantRetentionPolicyOperations', 'ServantRetentionPolicyValue', 'THREAD_POLICY_ID', 'ThreadPolicy', 'ThreadPolicyOperations', 'ThreadPolicyValue', '_ServantActivatorStub', '_ServantLocatorStub'
			),
		153 => array (
			'NoContext', 'NoContextHelper'
			),
		154 => array (
			'AdapterInactive', 'AdapterInactiveHelper', 'State'
			),
		155 => array (
			'AdapterAlreadyExists', 'AdapterAlreadyExistsHelper', 'AdapterNonExistent', 'AdapterNonExistentHelper', 'InvalidPolicy', 'InvalidPolicyHelper', 'NoServant', 'NoServantHelper', 'ObjectAlreadyActive', 'ObjectAlreadyActiveHelper', 'ObjectNotActive', 'ObjectNotActiveHelper', 'ServantAlreadyActive', 'ServantAlreadyActiveHelper', 'ServantNotActive', 'ServantNotActiveHelper', 'WrongAdapter', 'WrongAdapterHelper', 'WrongPolicy', 'WrongPolicyHelper'
			),
		156 => array (
			'CookieHolder'
			),
		157 => array (
			'RunTime', 'RunTimeOperations'
			),
		158 => array (
			'_Remote_Stub'
			),
		159 => array (
			'Attr', 'CDATASection', 'CharacterData', 'Comment', 'DOMConfiguration', 'DOMError', 'DOMErrorHandler', 'DOMException', 'DOMImplementation', 'DOMImplementationList', 'DOMImplementationSource', 'DOMStringList', 'DocumentFragment', 'DocumentType', 'EntityReference', 'NameList', 'NamedNodeMap', 'Node', 'NodeList', 'Notation', 'ProcessingInstruction', 'Text', 'TypeInfo', 'UserDataHandler'
			),
		160 => array (
			'DOMImplementationRegistry'
			),
		161 => array (
			'EventException', 'EventTarget', 'MutationEvent', 'UIEvent'
			),
		162 => array (
			'DOMImplementationLS', 'LSException', 'LSInput', 'LSLoadEvent', 'LSOutput', 'LSParser', 'LSParserFilter', 'LSProgressEvent', 'LSResourceResolver', 'LSSerializer', 'LSSerializerFilter'
			),
		163 => array (
			'DTDHandler', 'DocumentHandler', 'EntityResolver', 'ErrorHandler', 'HandlerBase', 'InputSource', 'Locator', 'SAXException', 'SAXNotRecognizedException', 'SAXNotSupportedException', 'SAXParseException', 'XMLFilter', 'XMLReader'
			),
		164 => array (
			'Attributes2', 'Attributes2Impl', 'DeclHandler', 'DefaultHandler2', 'EntityResolver2', 'LexicalHandler', 'Locator2', 'Locator2Impl'
			),
		165 => array (
			'AttributeListImpl', 'AttributesImpl', 'DefaultHandler', 'LocatorImpl', 'NamespaceSupport', 'ParserAdapter', 'ParserFactory', 'XMLFilterImpl', 'XMLReaderAdapter', 'XMLReaderFactory'
			),
		/* ambiguous class names (appear in more than one package) */
		166 => array (
			'Annotation', 'AnySeqHelper', 'Array', 'Attribute', 'AttributeList', 'AttributeSet', 'Attributes', 'AuthenticationException', 'Binding', 'Bounds', 'Certificate', 'CertificateEncodingException', 'CertificateException', 'CertificateExpiredException', 'CertificateNotYetValidException', 'CertificateParsingException', 'ConnectException', 'ContentHandler', 'Context', 'Control', 'Current', 'CurrentHelper', 'CurrentOperations', 'DOMLocator', 'DataInputStream', 'DataOutputStream', 'Date', 'DefaultLoaderRepository', 'Delegate', 'Document', 'DocumentEvent', 'DynAny', 'DynArray', 'DynEnum', 'DynFixed', 'DynSequence', 'DynStruct', 'DynUnion', 'DynValue', 'DynamicImplementation', 'Element', 'Entity', 'Event', 'EventListener', 'FieldNameHelper', 'FileFilter', 'Formatter', 'ForwardRequest', 'ForwardRequestHelper', 'InconsistentTypeCode', 'InputStream', 'IntrospectionException', 'InvalidAttributeValueException', 'InvalidKeyException', 'InvalidName', 'InvalidNameHelper', 'InvalidValue', 'List', 'MouseEvent',
			'NameValuePair', 'NameValuePairHelper', 'ORB', 'Object', 'ObjectIdHelper', 'ObjectImpl', 'OpenType', 'OutputStream', 'ParagraphView', 'Parser', 'Permission', 'Policy', 'Principal', 'Proxy', 'Reference', 'Statement', 'Timer', 'Timestamp', 'TypeMismatch', 'TypeMismatchHelper', 'UNKNOWN', 'UnknownHostException', 'X509Certificate'
			)
		),
	'SYMBOLS' => array(
		'(', ')', '[', ']', '{', '}', '*', '&', '%', '!', ';', '<', '>', '?'
		),
	'CASE_SENSITIVE' => array(
		GESHI_COMMENTS => true,
		/* all Java keywords are case sensitive */
		1 => true, 2 => true, 3 => true, 4 => true,
		5 => true, 6 => true, 7 => true, 8 => true, 9 => true,
		10 => true, 11 => true, 12 => true, 13 => true, 14 => true,
		15 => true, 16 => true, 17 => true, 18 => true, 19 => true,
		20 => true, 21 => true, 22 => true, 23 => true, 24 => true,
		25 => true, 26 => true, 27 => true, 28 => true, 29 => true,
		30 => true, 31 => true, 32 => true, 33 => true, 34 => true,
		35 => true, 36 => true, 37 => true, 38 => true, 39 => true,
		40 => true, 41 => true, 42 => true, 43 => true, 44 => true,
		45 => true, 46 => true, 47 => true, 48 => true, 49 => true,
		50 => true, 51 => true, 52 => true, 53 => true, 54 => true,
		55 => true, 56 => true, 57 => true, 58 => true, 59 => true,
		60 => true, 61 => true, 62 => true, 63 => true, 64 => true,
		65 => true, 66 => true, 67 => true, 68 => true, 69 => true,
		70 => true, 71 => true, 72 => true, 73 => true, 74 => true,
		75 => true, 76 => true, 77 => true, 78 => true, 79 => true,
		80 => true, 81 => true, 82 => true, 83 => true, 84 => true,
		85 => true, 86 => true, 87 => true, 88 => true, 89 => true,
		90 => true, 91 => true, 92 => true, 93 => true, 94 => true,
		95 => true, 96 => true, 97 => true, 98 => true, 99 => true,
		100 => true, 101 => true, 102 => true, 103 => true, 104 => true,
		105 => true, 106 => true, 107 => true, 108 => true, 109 => true,
		110 => true, 111 => true, 112 => true, 113 => true, 114 => true,
		115 => true, 116 => true, 117 => true, 118 => true, 119 => true,
		120 => true, 121 => true, 122 => true, 123 => true, 124 => true,
		125 => true, 126 => true, 127 => true, 128 => true, 129 => true,
		130 => true, 131 => true, 132 => true, 133 => true, 134 => true,
		135 => true, 136 => true, 137 => true, 138 => true, 139 => true,
		140 => true, 141 => true, 142 => true, 143 => true, 144 => true,
		145 => true, 146 => true, 147 => true, 148 => true, 149 => true,
		150 => true, 151 => true, 152 => true, 153 => true, 154 => true,
		155 => true, 156 => true, 157 => true, 158 => true, 159 => true,
		160 => true, 161 => true, 162 => true, 163 => true, 164 => true,
		165 => true, 166 => true
	),
	'STYLES' => array(
		'KEYWORDS' => array(
			1 => 'color: #b1b100;',
			2 => 'color: #000000; font-weight: bold;',
			3 => 'color: #993333;',
			4 => 'color: #b13366;',
			5 => 'color: #aaaadd; font-weight: bold;',
			6 => 'color: #aaaadd; font-weight: bold;',
			7 => 'color: #aaaadd; font-weight: bold;',
			8 => 'color: #aaaadd; font-weight: bold;',
			9 => 'color: #aaaadd; font-weight: bold;',
			10 => 'color: #aaaadd; font-weight: bold;',
			11 => 'color: #aaaadd; font-weight: bold;',
			12 => 'color: #aaaadd; font-weight: bold;',
			13 => 'color: #aaaadd; font-weight: bold;',
			14 => 'color: #aaaadd; font-weight: bold;',
			15 => 'color: #aaaadd; font-weight: bold;',
			16 => 'color: #aaaadd; font-weight: bold;',
			17 => 'color: #aaaadd; font-weight: bold;',
			18 => 'color: #aaaadd; font-weight: bold;',
			19 => 'color: #aaaadd; font-weight: bold;',
			20 => 'color: #aaaadd; font-weight: bold;',
			21 => 'color: #aaaadd; font-weight: bold;',
			22 => 'color: #aaaadd; font-weight: bold;',
			23 => 'color: #aaaadd; font-weight: bold;',
			24 => 'color: #aaaadd; font-weight: bold;',
			25 => 'color: #aaaadd; font-weight: bold;',
			26 => 'color: #aaaadd; font-weight: bold;',
			27 => 'color: #aaaadd; font-weight: bold;',
			28 => 'color: #aaaadd; font-weight: bold;',
			29 => 'color: #aaaadd; font-weight: bold;',
			30 => 'color: #aaaadd; font-weight: bold;',
			31 => 'color: #aaaadd; font-weight: bold;',
			32 => 'color: #aaaadd; font-weight: bold;',
			33 => 'color: #aaaadd; font-weight: bold;',
			34 => 'color: #aaaadd; font-weight: bold;',
			35 => 'color: #aaaadd; font-weight: bold;',
			36 => 'color: #aaaadd; font-weight: bold;',
			37 => 'color: #aaaadd; font-weight: bold;',
			38 => 'color: #aaaadd; font-weight: bold;',
			39 => 'color: #aaaadd; font-weight: bold;',
			40 => 'color: #aaaadd; font-weight: bold;',
			41 => 'color: #aaaadd; font-weight: bold;',
			42 => 'color: #aaaadd; font-weight: bold;',
			43 => 'color: #aaaadd; font-weight: bold;',
			44 => 'color: #aaaadd; font-weight: bold;',
			45 => 'color: #aaaadd; font-weight: bold;',
			46 => 'color: #aaaadd; font-weight: bold;',
			47 => 'color: #aaaadd; font-weight: bold;',
			48 => 'color: #aaaadd; font-weight: bold;',
			49 => 'color: #aaaadd; font-weight: bold;',
			50 => 'color: #aaaadd; font-weight: bold;',
			51 => 'color: #aaaadd; font-weight: bold;',
			52 => 'color: #aaaadd; font-weight: bold;',
			53 => 'color: #aaaadd; font-weight: bold;',
			54 => 'color: #aaaadd; font-weight: bold;',
			55 => 'color: #aaaadd; font-weight: bold;',
			56 => 'color: #aaaadd; font-weight: bold;',
			57 => 'color: #aaaadd; font-weight: bold;',
			58 => 'color: #aaaadd; font-weight: bold;',
			59 => 'color: #aaaadd; font-weight: bold;',
			60 => 'color: #aaaadd; font-weight: bold;',
			61 => 'color: #aaaadd; font-weight: bold;',
			62 => 'color: #aaaadd; font-weight: bold;',
			63 => 'color: #aaaadd; font-weight: bold;',
			64 => 'color: #aaaadd; font-weight: bold;',
			65 => 'color: #aaaadd; font-weight: bold;',
			66 => 'color: #aaaadd; font-weight: bold;',
			67 => 'color: #aaaadd; font-weight: bold;',
			68 => 'color: #aaaadd; font-weight: bold;',
			69 => 'color: #aaaadd; font-weight: bold;',
			70 => 'color: #aaaadd; font-weight: bold;',
			71 => 'color: #aaaadd; font-weight: bold;',
			72 => 'color: #aaaadd; font-weight: bold;',
			73 => 'color: #aaaadd; font-weight: bold;',
			74 => 'color: #aaaadd; font-weight: bold;',
			75 => 'color: #aaaadd; font-weight: bold;',
			76 => 'color: #aaaadd; font-weight: bold;',
			77 => 'color: #aaaadd; font-weight: bold;',
			78 => 'color: #aaaadd; font-weight: bold;',
			79 => 'color: #aaaadd; font-weight: bold;',
			80 => 'color: #aaaadd; font-weight: bold;',
			81 => 'color: #aaaadd; font-weight: bold;',
			82 => 'color: #aaaadd; font-weight: bold;',
			83 => 'color: #aaaadd; font-weight: bold;',
			84 => 'color: #aaaadd; font-weight: bold;',
			85 => 'color: #aaaadd; font-weight: bold;',
			86 => 'color: #aaaadd; font-weight: bold;',
			87 => 'color: #aaaadd; font-weight: bold;',
			88 => 'color: #aaaadd; font-weight: bold;',
			89 => 'color: #aaaadd; font-weight: bold;',
			90 => 'color: #aaaadd; font-weight: bold;',
			91 => 'color: #aaaadd; font-weight: bold;',
			92 => 'color: #aaaadd; font-weight: bold;',
			93 => 'color: #aaaadd; font-weight: bold;',
			94 => 'color: #aaaadd; font-weight: bold;',
			95 => 'color: #aaaadd; font-weight: bold;',
			96 => 'color: #aaaadd; font-weight: bold;',
			97 => 'color: #aaaadd; font-weight: bold;',
			98 => 'color: #aaaadd; font-weight: bold;',
			99 => 'color: #aaaadd; font-weight: bold;',
			100 => 'color: #aaaadd; font-weight: bold;',
			101 => 'color: #aaaadd; font-weight: bold;',
			102 => 'color: #aaaadd; font-weight: bold;',
			103 => 'color: #aaaadd; font-weight: bold;',
			104 => 'color: #aaaadd; font-weight: bold;',
			105 => 'color: #aaaadd; font-weight: bold;',
			106 => 'color: #aaaadd; font-weight: bold;',
			107 => 'color: #aaaadd; font-weight: bold;',
			108 => 'color: #aaaadd; font-weight: bold;',
			109 => 'color: #aaaadd; font-weight: bold;',
			110 => 'color: #aaaadd; font-weight: bold;',
			111 => 'color: #aaaadd; font-weight: bold;',
			112 => 'color: #aaaadd; font-weight: bold;',
			113 => 'color: #aaaadd; font-weight: bold;',
			114 => 'color: #aaaadd; font-weight: bold;',
			115 => 'color: #aaaadd; font-weight: bold;',
			116 => 'color: #aaaadd; font-weight: bold;',
			117 => 'color: #aaaadd; font-weight: bold;',
			118 => 'color: #aaaadd; font-weight: bold;',
			119 => 'color: #aaaadd; font-weight: bold;',
			120 => 'color: #aaaadd; font-weight: bold;',
			121 => 'color: #aaaadd; font-weight: bold;',
			122 => 'color: #aaaadd; font-weight: bold;',
			123 => 'color: #aaaadd; font-weight: bold;',
			124 => 'color: #aaaadd; font-weight: bold;',
			125 => 'color: #aaaadd; font-weight: bold;',
			126 => 'color: #aaaadd; font-weight: bold;',
			127 => 'color: #aaaadd; font-weight: bold;',
			128 => 'color: #aaaadd; font-weight: bold;',
			129 => 'color: #aaaadd; font-weight: bold;',
			130 => 'color: #aaaadd; font-weight: bold;',
			131 => 'color: #aaaadd; font-weight: bold;',
			132 => 'color: #aaaadd; font-weight: bold;',
			133 => 'color: #aaaadd; font-weight: bold;',
			134 => 'color: #aaaadd; font-weight: bold;',
			135 => 'color: #aaaadd; font-weight: bold;',
			136 => 'color: #aaaadd; font-weight: bold;',
			137 => 'color: #aaaadd; font-weight: bold;',
			138 => 'color: #aaaadd; font-weight: bold;',
			139 => 'color: #aaaadd; font-weight: bold;',
			140 => 'color: #aaaadd; font-weight: bold;',
			141 => 'color: #aaaadd; font-weight: bold;',
			142 => 'color: #aaaadd; font-weight: bold;',
			143 => 'color: #aaaadd; font-weight: bold;',
			144 => 'color: #aaaadd; font-weight: bold;',
			145 => 'color: #aaaadd; font-weight: bold;',
			146 => 'color: #aaaadd; font-weight: bold;',
			147 => 'color: #aaaadd; font-weight: bold;',
			148 => 'color: #aaaadd; font-weight: bold;',
			149 => 'color: #aaaadd; font-weight: bold;',
			150 => 'color: #aaaadd; font-weight: bold;',
			151 => 'color: #aaaadd; font-weight: bold;',
			152 => 'color: #aaaadd; font-weight: bold;',
			153 => 'color: #aaaadd; font-weight: bold;',
			154 => 'color: #aaaadd; font-weight: bold;',
			155 => 'color: #aaaadd; font-weight: bold;',
			156 => 'color: #aaaadd; font-weight: bold;',
			157 => 'color: #aaaadd; font-weight: bold;',
			158 => 'color: #aaaadd; font-weight: bold;',
			159 => 'color: #aaaadd; font-weight: bold;',
			160 => 'color: #aaaadd; font-weight: bold;',
			161 => 'color: #aaaadd; font-weight: bold;',
			162 => 'color: #aaaadd; font-weight: bold;',
			163 => 'color: #aaaadd; font-weight: bold;',
			164 => 'color: #aaaadd; font-weight: bold;',
			165 => 'color: #aaaadd; font-weight: bold;',
			166 => 'color: #aaaadd; font-weight: bold;'
			),
		'COMMENTS' => array(
			1 => 'color: #808080; font-style: italic;',
			'MULTI' => 'color: #808080; font-style: italic;'
			),
		'ESCAPE_CHAR' => array(
			0 => 'color: #000099; font-weight: bold;'
			),
		'BRACKETS' => array(
			0 => 'color: #66cc66;'
			),
		'STRINGS' => array(
			0 => 'color: #ff0000;'
			),
		'NUMBERS' => array(
			0 => 'color: #cc66cc;'
			),
		'METHODS' => array(
			1 => 'color: #006600;',
			2 => 'color: #006600;'
			),
		'SYMBOLS' => array(
			0 => 'color: #66cc66;'
			),
		'SCRIPT' => array(
			),
		'REGEXPS' => array(
			)
		),
	'URLS' => array(
		1 => '',
		2 => '',
		3 => '',
		4 => '',
		5 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/applet/{FNAME}.html',
		6 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/{FNAME}.html',
		7 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/color/{FNAME}.html',
		8 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/datatransfer/{FNAME}.html',
		9 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/dnd/{FNAME}.html',
		10 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/event/{FNAME}.html',
		11 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/font/{FNAME}.html',
		12 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/geom/{FNAME}.html',
		13 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/im/{FNAME}.html',
		14 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/im/spi/{FNAME}.html',
		15 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/image/{FNAME}.html',
		16 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/image/renderable/{FNAME}.html',
		17 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/awt/print/{FNAME}.html',
		18 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/beans/{FNAME}.html',
		19 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/beans/beancontext/{FNAME}.html',
		20 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/io/{FNAME}.html',
		21 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/lang/{FNAME}.html',
		22 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/lang/annotation/{FNAME}.html',
		23 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/lang/instrument/{FNAME}.html',
		24 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/lang/management/{FNAME}.html',
		25 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/lang/ref/{FNAME}.html',
		26 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/lang/reflect/{FNAME}.html',
		27 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/math/{FNAME}.html',
		28 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/net/{FNAME}.html',
		29 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/nio/{FNAME}.html',
		30 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/nio/channels/{FNAME}.html',
		31 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/nio/channels/spi/{FNAME}.html',
		32 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/nio/charset/{FNAME}.html',
		33 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/nio/charset/spi/{FNAME}.html',
		34 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/rmi/{FNAME}.html',
		35 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/rmi/activation/{FNAME}.html',
		36 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/rmi/dgc/{FNAME}.html',
		37 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/rmi/registry/{FNAME}.html',
		38 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/rmi/server/{FNAME}.html',
		39 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/security/{FNAME}.html',
		40 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/security/acl/{FNAME}.html',
		41 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/security/cert/{FNAME}.html',
		42 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/security/interfaces/{FNAME}.html',
		43 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/security/spec/{FNAME}.html',
		44 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/sql/{FNAME}.html',
		45 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/text/{FNAME}.html',
		46 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/{FNAME}.html',
		47 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/concurrent/{FNAME}.html',
		48 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/concurrent/atomic/{FNAME}.html',
		49 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/concurrent/locks/{FNAME}.html',
		50 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/jar/{FNAME}.html',
		51 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/logging/{FNAME}.html',
		52 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/prefs/{FNAME}.html',
		53 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/{FNAME}.html',
		54 => 'http://java.sun.com/j2se/1.5.0/docs/api/java/util/zip/{FNAME}.html',
		55 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/accessibility/{FNAME}.html',
		56 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/activity/{FNAME}.html',
		57 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/crypto/{FNAME}.html',
		58 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/crypto/interfaces/{FNAME}.html',
		59 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/crypto/spec/{FNAME}.html',
		60 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/imageio/{FNAME}.html',
		61 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/imageio/event/{FNAME}.html',
		62 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/imageio/metadata/{FNAME}.html',
		63 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/imageio/plugins/bmp/{FNAME}.html',
		64 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/imageio/plugins/jpeg/{FNAME}.html',
		65 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/imageio/spi/{FNAME}.html',
		66 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/imageio/stream/{FNAME}.html',
		67 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/{FNAME}.html',
		68 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/loading/{FNAME}.html',
		69 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/modelmbean/{FNAME}.html',
		70 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/monitor/{FNAME}.html',
		71 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/openmbean/{FNAME}.html',
		72 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/relation/{FNAME}.html',
		73 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/remote/{FNAME}.html',
		74 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/remote/rmi/{FNAME}.html',
		75 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/management/timer/{FNAME}.html',
		76 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/naming/{FNAME}.html',
		77 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/naming/directory/{FNAME}.html',
		78 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/naming/event/{FNAME}.html',
		79 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/naming/ldap/{FNAME}.html',
		80 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/naming/spi/{FNAME}.html',
		81 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/net/{FNAME}.html',
		82 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/net/ssl/{FNAME}.html',
		83 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/print/{FNAME}.html',
		84 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/print/attribute/{FNAME}.html',
		85 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/print/attribute/standard/{FNAME}.html',
		86 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/print/event/{FNAME}.html',
		87 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/rmi/{FNAME}.html',
		88 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/rmi/CORBA/{FNAME}.html',
		89 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/rmi/ssl/{FNAME}.html',
		90 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/security/auth/{FNAME}.html',
		91 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/security/auth/callback/{FNAME}.html',
		92 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/security/auth/kerberos/{FNAME}.html',
		93 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/security/auth/login/{FNAME}.html',
		94 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/security/auth/spi/{FNAME}.html',
		95 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/security/auth/x500/{FNAME}.html',
		96 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/security/sasl/{FNAME}.html',
		97 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/sound/midi/{FNAME}.html',
		98 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/sound/midi/spi/{FNAME}.html',
		99 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/sound/sampled/{FNAME}.html',
		100 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/sound/sampled/spi/{FNAME}.html',
		101 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/sql/{FNAME}.html',
		102 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/sql/rowset/{FNAME}.html',
		103 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/sql/rowset/serial/{FNAME}.html',
		104 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/sql/rowset/spi/{FNAME}.html',
		105 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/{FNAME}.html',
		106 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/border/{FNAME}.html',
		107 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/colorchooser/{FNAME}.html',
		108 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/event/{FNAME}.html',
		109 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/filechooser/{FNAME}.html',
		110 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/plaf/{FNAME}.html',
		111 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/plaf/basic/{FNAME}.html',
		112 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/plaf/metal/{FNAME}.html',
		113 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/plaf/multi/{FNAME}.html',
		114 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/plaf/synth/{FNAME}.html',
		115 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/table/{FNAME}.html',
		116 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/text/{FNAME}.html',
		117 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/text/html/{FNAME}.html',
		118 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/text/html/parser/{FNAME}.html',
		119 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/text/rtf/{FNAME}.html',
		120 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/tree/{FNAME}.html',
		121 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/undo/{FNAME}.html',
		122 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/transaction/{FNAME}.html',
		123 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/transaction/xa/{FNAME}.html',
		124 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/{FNAME}.html',
		125 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/datatype/{FNAME}.html',
		126 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/namespace/{FNAME}.html',
		127 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/parsers/{FNAME}.html',
		128 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/transform/{FNAME}.html',
		129 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/transform/dom/{FNAME}.html',
		130 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/transform/sax/{FNAME}.html',
		131 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/transform/stream/{FNAME}.html',
		132 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/validation/{FNAME}.html',
		133 => 'http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/xpath/{FNAME}.html',
		134 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/ietf/jgss/{FNAME}.html',
		135 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/CORBA/{FNAME}.html',
		136 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/CORBA/DynAnyPackage/{FNAME}.html',
		137 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/CORBA/TypeCodePackage/{FNAME}.html',
		138 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/CORBA/portable/{FNAME}.html',
		139 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/CosNaming/{FNAME}.html',
		140 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/CosNaming/NamingContextExtPackage/{FNAME}.html',
		141 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/CosNaming/NamingContextPackage/{FNAME}.html',
		142 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/Dynamic/{FNAME}.html',
		143 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/DynamicAny/{FNAME}.html',
		144 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/DynamicAny/DynAnyFactoryPackage/{FNAME}.html',
		145 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/DynamicAny/DynAnyPackage/{FNAME}.html',
		146 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/IOP/{FNAME}.html',
		147 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/IOP/CodecFactoryPackage/{FNAME}.html',
		148 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/IOP/CodecPackage/{FNAME}.html',
		149 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/Messaging/{FNAME}.html',
		150 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/PortableInterceptor/{FNAME}.html',
		151 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/PortableInterceptor/ORBInitInfoPackage/{FNAME}.html',
		152 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/PortableServer/{FNAME}.html',
		153 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/PortableServer/CurrentPackage/{FNAME}.html',
		154 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/PortableServer/POAManagerPackage/{FNAME}.html',
		155 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/PortableServer/POAPackage/{FNAME}.html',
		156 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/PortableServer/ServantLocatorPackage/{FNAME}.html',
		157 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/SendingContext/{FNAME}.html',
		158 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/omg/stub/java/rmi/{FNAME}.html',
		159 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/w3c/dom/{FNAME}.html',
		160 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/w3c/dom/bootstrap/{FNAME}.html',
		161 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/w3c/dom/events/{FNAME}.html',
		162 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/w3c/dom/ls/{FNAME}.html',
		163 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/xml/sax/{FNAME}.html',
		164 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/xml/sax/ext/{FNAME}.html',
		165 => 'http://java.sun.com/j2se/1.5.0/docs/api/org/xml/sax/helpers/{FNAME}.html',
		/* ambiguous class names (appear in more than one package) */
		166 => 'http://www.google.com/search?sitesearch=java.sun.com&amp;q=allinurl%3Aj2se%2F1+5+0%2Fdocs%2Fapi+{FNAME}'
		),
	'OOLANG' => true,
	'OBJECT_SPLITTERS' => array(
		1 => '.'
		/* Java does not use '::' */
		),
	'REGEXPS' => array(
		),
	'STRICT_MODE_APPLIES' => GESHI_NEVER,
	'SCRIPT_DELIMITERS' => array(
		),
	'HIGHLIGHT_STRICT_BLOCK' => array(
		)
);

?>
