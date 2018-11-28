package org.zebra.RFIDSample4App;

import com.mot.rfid.api3.*;
import com.mot.rfid.api3.PreFilters.PreFilter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.concurrent.locks.*;
import java.util.Calendar;

public class RFIDSample4App {
	RFIDReader myReader = null;
	private boolean accessComplete = false;
        private boolean inventoryComplete = false;
        
        private Lock accessEventLock = new ReentrantLock();
        private Condition accessEventCondVar = accessEventLock.newCondition();
        
        private Lock inventoryStopEventLock = new ReentrantLock();
        private Condition inventoryStopCondVar = inventoryStopEventLock.newCondition();
	
	public static Hashtable<String,Long> tagStore = null;
	
	public static final String API_SUCCESS = "Function Succeeded";
	public static final String PARAM_ERROR = "Parameter Error";
	final String APP_NAME = "J_RFIDSample3";
	
	public boolean isConnected;
	public String hostName = "";
	public int port = 5084;
	
	String[] memoryBank = new String[] { "Reserved", "EPC", "TID", "USER" };
	
	public boolean isAccessSequenceRunning = false;
	String[] tagState = new String[] { "New", "Gone", "Back", "None" };
	
	// To display tag read count
	public long uniqueTags = 0;
	public long totalTags = 0;
	
	private EventsHandler eventsHandler = new EventsHandler();
	
	// Antennas
	
	public Antennas antennas;
	
	// Access Filter
	public AccessFilter accessFilter = null;
	public boolean isAccessFilterSet = false;

	// Post Filter
	public PostFilter postFilter = null;
	public boolean isPostFilterSet = false;

	// Antenna Info
	public AntennaInfo antennaInfo = null;

	// Pre Filter
	public PreFilters preFilters = null;
	
	public PreFilters.PreFilter preFilter1 = null;
	public PreFilters.PreFilter preFilter2 = null;

	public String preFilterTagPattern1 = null;
	public String preFilterTagPattern2 = null;
	
	public boolean isPreFilterSet1 = false;
	public boolean isPreFilterSet2 = false;
        public int preFilterActionIndex1 = 0;
        public int preFilterActionIndex2 = 0;
    
        public TriggerInfo triggerInfo = null;
	
	public int readerTypeIndex = 1;
	
    // Access Params
	TagAccess tagAccess = null;
	TagAccess.ReadAccessParams readAccessParams;
	TagAccess.WriteAccessParams writeAccessParams;
	TagAccess.LockAccessParams lockAccessParams;
	TagAccess.KillAccessParams killAccessParams;
	
	// Access filter

	BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
	public int rowId = 0;

	TagData[] myTags = null;
	
	
	public RFIDSample4App()
	{
		// Create Reader Object
		myReader = new RFIDReader();
		
		// Hash table to hold the tag data
		tagStore = new Hashtable();
		isAccessSequenceRunning = false;
		
		// Create the Access Filter
		accessFilter = new AccessFilter();
		accessFilter.setAccessFilterMatchPattern(FILTER_MATCH_PATTERN.A);
		accessFilter.TagPatternA = null;
		accessFilter.TagPatternB = null;

		// create the post filter
		postFilter = new PostFilter();

		// Create Antenna Info
		antennaInfo = new AntennaInfo();
		
		// Create Pre-Filter
		preFilters = new PreFilters();
   
		preFilter1 = preFilters.new PreFilter();
	    preFilter2 = preFilters.new PreFilter();
		
		antennas = myReader.Config.Antennas;

		triggerInfo = new TriggerInfo();
		
		triggerInfo.StartTrigger
				.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
		triggerInfo.StopTrigger
				.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);

		triggerInfo.TagEventReportInfo
				.setReportNewTagEvent(TAG_EVENT_REPORT_TRIGGER.MODERATED);
		triggerInfo.TagEventReportInfo
				.setNewTagEventModeratedTimeoutMilliseconds((short) 500);

		triggerInfo.TagEventReportInfo
				.setReportTagInvisibleEvent(TAG_EVENT_REPORT_TRIGGER.MODERATED);
		triggerInfo.TagEventReportInfo
				.setTagInvisibleEventModeratedTimeoutMilliseconds((short) 500);

		triggerInfo.TagEventReportInfo
				.setReportTagBackToVisibilityEvent(TAG_EVENT_REPORT_TRIGGER.MODERATED);
		triggerInfo.TagEventReportInfo
				.setTagBackToVisibilityModeratedTimeoutMilliseconds((short) 500);

		triggerInfo.setTagReportTrigger(1);
		
		// Access Params
		
		tagAccess = new TagAccess();
		readAccessParams  = tagAccess.new ReadAccessParams();
		writeAccessParams = tagAccess.new WriteAccessParams();
		lockAccessParams  = tagAccess.new LockAccessParams();
		killAccessParams  = tagAccess.new KillAccessParams();
		
				
		// On Device, connect automatically to the reader
		connectToReader("127.0.0.1", 5084);
			
		
		
		
	}
	
	
	public void Createmenu()
    {
        int option = 0;
        Boolean keepWorking = true;
       
       
        while (keepWorking)
        {
            System.out.println("..................................................");
            System.out.println("Welcome to RFID API3 Java Standard Sample Application");
            System.out.println("..................................................\n\n");

            System.out.println("----Command Menu----");
            System.out.println("1. Capability");
            System.out.println("2. Configuration");
            System.out.println("3. Inventory");
            System.out.println("4. Access");
            System.out.println("5. Exit");
            
            

            try
            {
                option =  Integer.valueOf(inputReader.readLine());

                switch (option)
                {
                    // Capability
                    case 1:
                        DisplayCapability();
                        break;
                    // Configuration
                    case 2:
                        ConfigurationMenu();
                        break;
                    // Inventory operation
                    case 3:
                        InventoryMenu();
                        break;
                    // Access operation
                    case 4:
                        AccessMenu();
                        break;
                    // Application Exit
                    case 5:
                    	myReader.disconnect();
                        keepWorking = false;
                        break;
                }
            }
            catch (Exception ex)
            {
            	System.out.println(ex.getMessage());
            }
        }
    }
	
	private void DisplayCapability()
	{
		
		 System.out.println("Reader Capabilities\n\n");
         System.out.println("FirwareVersion="+myReader.ReaderCapabilities.getFirwareVersion());
         System.out.println("ModelName= "+myReader.ReaderCapabilities.getModelName());
         System.out.println("NumAntennaSupported= "+myReader.ReaderCapabilities.getNumAntennaSupported());
         System.out.println("NumGPIPorts= "+myReader.ReaderCapabilities.getNumGPIPorts());
         System.out.println("NumGPOPorts= "+myReader.ReaderCapabilities.getNumGPOPorts());
         System.out.println("IsUTCClockSupported= "+myReader.ReaderCapabilities.isUTCClockSupported());
         System.out.println("IsBlockEraseSupported= "+myReader.ReaderCapabilities.isBlockEraseSupported());
         System.out.println("IsBlockWriteSupported= "+myReader.ReaderCapabilities.isBlockWriteSupported());
         System.out.println("IsTagInventoryStateAwareSingulationSupported= "+myReader.ReaderCapabilities.isTagInventoryStateAwareSingulationSupported());
         System.out.println("MaxNumOperationsInAccessSequence= "+myReader.ReaderCapabilities.getMaxNumOperationsInAccessSequence());
         System.out.println("MaxNumPreFilters= "+myReader.ReaderCapabilities.getMaxNumPreFilters());
         System.out.println("CommunicationStandard= "+myReader.ReaderCapabilities.getCommunicationStandard());
         System.out.println("CountryCode= "+myReader.ReaderCapabilities.getCountryCode());
         System.out.println("IsHoppingEnabled= "+myReader.ReaderCapabilities.isHoppingEnabled());
		
	}
	
	
	
	private void ConfigurationMenu()
	{
		  int option = 0;
          Boolean keepworking = true;
          while (keepworking)
          {
             System.out.println("");
             System.out.println("----Configuration Sub Menu---");
             System.out.println("1. Get Singulation Control");
             System.out.println("2. GPO");
             System.out.println("3. GPI");
             System.out.println("4. Antenna Config");
             System.out.println("5. RF Mode ");
             System.out.println("6. Back to Main Menu");
             
             try
             {
             
	             option = Integer.valueOf(inputReader.readLine());
	             switch (option)
	             {
	                  case 1:
	                      GetSingulationControl();
	                      break;
	                  case 2:
	                      ConfigureGPOState();
	                      break;
	                  case 3:
	                      ConfigureGPIState();
	                      break;
	                  case 4:
	                      ConfigureAntenna();
	                      break;
	                  case 5:
	                      ConfigureRFMode();
	                      break;
	                  case 6:
	                      keepworking = false;
	                      break;
	                  default:
	                	  System.out.println("Enter a valid Integer in the range 1-6");
	                	  break;
	              }
             }
             catch(IOException ioE)
             {
            	 System.out.println("Enter a valid Integer in the range 1-6");
            	 
             }
          }
		
	}
	
	private void GetSingulationControl()
	{
		
		 int antennaID = 0;
         Antennas.SingulationControl g1_singulationControl;
         try
         {
            System.out.println("Enter AntennaID");
            antennaID = Integer.valueOf(inputReader.readLine());
            g1_singulationControl = myReader.Config.Antennas.getSingulationControl(antennaID);

            System.out.println("Session: "+g1_singulationControl.getSession());
            System.out.println("TagPopulation: "+g1_singulationControl.getTagPopulation());
            System.out.println("TagTransitTime: "+g1_singulationControl.getTagTransitTime());
            System.out.println("inventoryState: "+g1_singulationControl.Action.getInventoryState());
            System.out.println("SLFlag: "+g1_singulationControl.Action.getSLFlag());
            System.out.println("PerformStateAwareSingulationAction: "+g1_singulationControl.Action.getInventoryState());
            
         }
         catch(NumberFormatException nfe)
         {
        	 System.out.println("Invalid Input Format");
        	 
         }
         catch(InvalidUsageException iue)
         {
        	 System.out.println("GetSingulationControl failed.Reason: "+iue.getInfo());
         }
         catch(OperationFailureException ofe)
         {
        	 System.out.println("GetSingulationControl failed.Reason: "+ofe.getVendorMessage());
         }
         catch (Exception ex)
         {
        	System.out.println(ex.toString());
         }
		
	}
	
	private void ConfigureGPOState()
	{
	       Integer portNumber;
           Byte GPOState = 0;
           Integer option = 0;
           Boolean keepworking = true;
           while(keepworking)
           {
               System.out.println("----Command Menu----");
               System.out.println("1. SetGPOState");
               System.out.println("2. GetGPOState");
               System.out.println("");
               System.out.println("3. Go back");
               try
               {
                   option = Integer.valueOf(inputReader.readLine());
                   switch (option)
                   {
                       case 1:
                    	   System.out.println("Enter portNumber");
                           portNumber = Integer.valueOf(inputReader.readLine());
                           System.out.println("Enter GPOState..0 to disable,1 to enable:");
                           GPOState = Byte.valueOf(inputReader.readLine());
                           if (GPOState != 0)
                               myReader.Config.GPO.setPortState(portNumber, GPO_PORT_STATE.TRUE);
                           else
                        	   myReader.Config.GPO.setPortState(portNumber,GPO_PORT_STATE.FALSE);
                        	   System.out.println("Set GPO Successfully");
                           break;
                       case 2:
                    	   System.out.println("Enter portNumber");
                    	   portNumber = Integer.valueOf(inputReader.readLine());
                           if (GPO_PORT_STATE.TRUE == myReader.Config.GPO.getPortState(portNumber))
                        	   System.out.println("GPOEnable= TRUE");
                           else
                        	   System.out.println("GPOEnable= FALSE");
                           break;
                       default:
                           keepworking = false;
                           break;
                   }
               }
               catch(NumberFormatException nfe)
               {
              	 System.out.println("Invalid Input Format");
              	 
               }
               catch(InvalidUsageException iue)
               {
        	 System.out.println("Operation failed.Reason: "+iue.getInfo());
               }
               catch(OperationFailureException ofe)
               {
              	 System.out.println("Operation Failed.Reason: "+ofe.getVendorMessage());
               }
               catch (Exception ex)
               {
            	   System.out.println(ex.getMessage());
               }
           }
	}
	
	private void ConfigureGPIState()
	{
		Integer portNumber;
		
		try
        {
			System.out.println("Enter portNumber");
            portNumber = Integer.valueOf(inputReader.readLine());
            
            System.out.println("Port: "+portNumber.toString()+ "  Enabled: "+ myReader.Config.GPI.isPortEnabled(portNumber));
            
        }
        catch(NumberFormatException nfe)
        {
          System.out.println("Invalid Input Format");

        }
        catch(InvalidUsageException iue)
        {
          System.out.println("Operation failed.Reason: "+iue.getInfo());
        }
        catch (OperationFailureException opex)
        {
                System.out.println("Failed to get the port state. Reason: "+opex.getVendorMessage());
        }
        catch (Exception ex)
        {
        	System.out.println(ex.toString());
        }
		 
	}
	

	private void ConfigureAntenna()
	{
	    Integer antennaID,receiveSensitivity,transmitPowerIndex,transmitFrequencyIndex;
        Boolean keepworking = true;
        Antennas antennas = myReader.Config.Antennas;
        Antennas.Config antennaConfig;
        Integer option = 0;
        
        while (keepworking)
    	{
    		System.out.println("----Command Menu----");
            System.out.println("1. SetAntennaConfig");
            System.out.println("2. GetAntennaConfig");
            System.out.println("");
            System.out.println("3. Go back");
            try
            {
              option = Integer.valueOf(inputReader.readLine());
              switch (option)
              {
                case 1:
                   {  
                    	System.out.println("Enter AntennaID");
                        antennaID = Integer.valueOf(inputReader.readLine());
                        
                        antennaConfig = antennas.getAntennaConfig(antennaID);
                                          
                        System.out.println("Enter ReceiveSensitivityIndex  value ");
                        antennaConfig.setReceiveSensitivityIndex(Short.valueOf(inputReader.readLine()));
                       
                        System.out.println("Enter TransmitPowerIndex  value ");
                        antennaConfig.setTransmitPowerIndex(Short.valueOf(inputReader.readLine()));

                        System.out.println("Enter TransmitFrequencyIndex value ");
                        antennaConfig.setTransmitFrequencyIndex(Short.valueOf(inputReader.readLine()));
                       
                        antennas.setAntennaConfig(antennaID, antennaConfig);
                        System.out.println("Set Antenna Configuration Successfully");
                                      
                    }
                    break;
                    case 2:
                        
                        System.out.println("Enter AntennaID");
                        antennaID = Integer.valueOf(inputReader.readLine());
                        antennaConfig = antennas.getAntennaConfig(antennaID);

                        System.out.println("ReceiveSensitivityIndex: "+antennaConfig.getReceiveSensitivityIndex());
                        System.out.println("TransmitPowerIndex: "+antennaConfig.getTransmitPowerIndex());
                        System.out.println("TransmitFrequencyIndex: "+antennaConfig.getTransmitFrequencyIndex());

                        break;
                    case 3:
                        keepworking = false;
                        break;
                    default:
                    	 System.out.println("Enter a valid integer in the range 1-3");
                    	break;
                }
            }
            catch (NumberFormatException nfe)
            {
            	System.out.println("Invalid Input format "+nfe.getMessage());
            }
            catch (InvalidUsageException iue)
            {
            	System.out.println("Invalid Usage exception  "+iue.getInfo());
            }
            
            catch (OperationFailureException opEx)
            {
                System.out.println("Antenna Configuration failed.Reason: "+opEx.getVendorMessage());
            }
            catch (Exception ex)
            {
                System.out.println(ex.toString());
           	}
    	}
        	
      }
          
   	private void ConfigureRFMode()
	{
		
	      int antennaID;
          Antennas.RFMode rfMode;

          int option = 0;
          Boolean keepworking = true;
          System.out.println("Enter AntennaID: ");
                   
          while(keepworking)
          {
              
        	  try
              {
            	  antennaID = Integer.valueOf(inputReader.readLine());
            	  if(antennaID<0 || antennaID>myReader.ReaderCapabilities.getNumAntennaSupported())
            	  {
            		  System.out.println("Enter a valid AntennaID in the range 0-"+myReader.ReaderCapabilities.getNumAntennaSupported()+":");
            		  continue;
            	  }
              }
        	  catch(IOException ioE)
        	  {
        		  System.out.println("Enter a valid AntennaID in the range 0-"+myReader.ReaderCapabilities.getNumAntennaSupported()+":");
        		  continue;
        	  }
        	  
        	  System.out.println("----Command Menu----");
        	  System.out.println("1. SetRFMode");
        	  System.out.println("2. GetRFMode");
        	  System.out.println("");
        	  System.out.println("3. Go back");
             
        	  try
              {
                  option = Integer.valueOf(inputReader.readLine());

                  switch (option)
                  {
                      case 1:
                          {
                              
                          	  rfMode = myReader.Config.Antennas.getRFMode(antennaID);
                        	  
                        	  System.out.println("Enter RfModeTable Index  value ");
                        	  rfMode.setTableIndex(Integer.valueOf(inputReader.readLine()));
                              System.out.println("Enter Tari value ");
                              rfMode.setTari(Integer.valueOf(inputReader.readLine()));
                              myReader.Config.Antennas.setRFMode(antennaID,rfMode);
                              System.out.println("Set RF Mode Successfully");
                              
                            
                             
                          }
                          break;
                      case 2:
                          { 
                        	  
                        	  rfMode = myReader.Config.Antennas.getRFMode(antennaID);
                        	  System.out.println("RF ModeTable index: "+rfMode.getTableIndex()+"Tari value: "+rfMode.getTari());
                         }
                          break;
                      case 3:
                          keepworking = false;
                          break;
                       default:
                    	   System.out.println("Enter a valid Integer in the range 1-3");
                    	   break;

                  }
              }
        	  catch (NumberFormatException nfe)
              {
            	  System.out.println(nfe.getMessage());
            	  
              }
              catch (OperationFailureException opEx)
              {
                  System.out.println("Operation failed.Reason: "+opEx.getStatusDescription()+" Vendor message: "+opEx.getVendorMessage());
              }

              catch (IOException ex)
              {
            	  System.out.println("IO Exception"+ex.getMessage());
              }
        	  catch(Exception ex)
        	  {
        		  System.out.println("Caught Exception:"+ex.getMessage());
        	  }
              
              
           }
		
	}
	private void AccessOperationWithEPCID()
	{
		 Integer option;String tagID;
         Boolean keepworking = true;
         
                 
         while (keepworking)
         {
             System.out.println("");
             System.out.println("----Command Menu----");
             System.out.println("1. Read Tag");
             System.out.println("2. Write Tag");
             System.out.println("3. Lock Tag");
             System.out.println("4. Kill Tag");
             System.out.println("5. Go back to Access Menu");
             
             try
             {

	             option = Integer.valueOf(inputReader.readLine());
	                   
	             switch (option)
	             {
	                 case 1:
	                	 
	                	 TagData readAccessTag;
	                	   
	                	 System.out.println("Enter TagID");
	           	         tagID = inputReader.readLine(); 
	                     
	           	         getReadAccessParams();
	           	                      	           
	           	         readAccessTag = myReader.Actions.TagAccess.readWait(tagID,readAccessParams,null);
	                     System.out.println("Read-Data  : " + readAccessTag.getMemoryBankData());
	                	 
	                    break;
	                    
	                 case 2:
	                	 System.out.println("Enter TagID");
	           	         tagID = inputReader.readLine(); 
	           	         
	           	         getWriteAccessParams();
	           	         
	           	         myReader.Actions.TagAccess.writeWait(tagID, writeAccessParams, null);
	                     System.out.println("Data witten on tag successfully");
	           	         
	                     break;
	                 case 3:
	                	 System.out.println("Enter TagID");
	           	         tagID = inputReader.readLine(); 
	           	         
	           	         getLockAccessParams();
	           	         
	           	         myReader.Actions.TagAccess.lockWait(tagID, lockAccessParams, null);
	                     System.out.println("Tag locked successfully");
	           	         
	                     break;
	                 case 4:
	                	 System.out.println("Enter TagID");
	           	         tagID = inputReader.readLine(); 
	           	         
	           	         getKillAccessParams();
	           	         
	           	         myReader.Actions.TagAccess.killWait(tagID, killAccessParams, null);
	           	         System.out.println("Tag killed successfully");
	           	                          
	                     break;
	                 default:
	                     keepworking = false;
	                     break;
	             }
             }
             catch(NumberFormatException nfe)
             { 
             	 System.out.println("Invalid format"+nfe.getMessage());
             }
           
             catch(InvalidUsageException iue)
             {
             	  System.out.println("Invalid usage"+iue.getInfo());
             }
             catch (OperationFailureException opex)
             {
                 System.out.println("Access failed. Reason: " + opex.getVendorMessage());
             }
             catch(IOException ioe)
             {
             	  System.out.println("IO Exception"+ioe.getMessage());
             }
             catch (Exception ex)
             {
                 System.out.println(ex.toString());
             }
             
         }
	}
	
    	
	private void getReadAccessParams() throws NumberFormatException,IOException
    {
        Integer memoryBank; 
        
      	String tagID;
    	       
              
        System.out.println("Enter accessPassword");
        readAccessParams.setAccessPassword(Long.parseLong(inputReader.readLine(),16));
        
        System.out.println("Enter memoryBank ");
        System.out.println("0 for RESERVED ");
        System.out.println("1 for EPC ");
        System.out.println("2 for TID ");
        System.out.println("3 for USER ");
    
   

    	memoryBank = Integer.valueOf(inputReader.readLine());
    	readAccessParams.setMemoryBank(MEMORY_BANK.GetMemoryBankValue(memoryBank));
        
        System.out.println("Enter byte offset ");
        readAccessParams.setByteOffset(Integer.valueOf(inputReader.readLine()));
       
        System.out.println("Enter byte length ");
        readAccessParams.setByteCount(Integer.valueOf(inputReader.readLine()));

    }
	
	private void getWriteAccessParams() throws NumberFormatException,IOException,OperationFailureException,InvalidUsageException
	{
		Integer memoryBank;
        String writeData;
               	
	    System.out.println("Enter accessPassword");
        writeAccessParams.setAccessPassword(Long.parseLong(inputReader.readLine(),16));
        
        System.out.println("Enter memoryBank ");
        System.out.println("0 for RESERVED ");
        System.out.println("1 for EPC ");
        System.out.println("2 for TID ");
        System.out.println("3 for USER ");
        
        memoryBank = Integer.valueOf(inputReader.readLine());
        writeAccessParams.setMemoryBank(MEMORY_BANK.GetMemoryBankValue(memoryBank));
        
        System.out.println("Enter byte offset ");
        writeAccessParams.setByteOffset(Integer.valueOf(inputReader.readLine()));
        
        System.out.println("Enter data to be written ");
        writeData = inputReader.readLine();
        
        byte [] writeUserData = hexStringToByteArray(writeData);
        writeAccessParams.setWriteData(writeUserData);
        writeAccessParams.setWriteDataLength(writeUserData.length);
        
            
       
	}
	
	private void getLockAccessParams() throws NumberFormatException,IOException
	{
		
	    Integer temp,dataFieldInt,privilegeInt;
        String tagID;
	       
        System.out.println("Enter TagID");
	    tagID = inputReader.toString(); 
           
        System.out.println("Enter accessPassword");
        lockAccessParams.setAccessPassword(Long.parseLong(inputReader.readLine(),16));
    
        System.out.println("Enter memory to be locked..");
        System.out.println("0 for Kill Password ");
        System.out.println("1 for Access Password ");
        System.out.println("2 for EPC Memory ");
        System.out.println("3 for TID Memory ");
        System.out.println("4 for User  Memory ");
        dataFieldInt = Integer.valueOf(inputReader.readLine());
                    
        System.out.println("Enter the locking priviledge ");
        System.out.println("0 for PRIVILEGE_NONE ");
        System.out.println("1 for PRIVILEGE_READ_WRITE");
        System.out.println("2 for PRIVILEGE_PERMA_LOCK ");
        System.out.println("3 for PRIVILEGE_PERMA_UNLOCK ");
        System.out.println("4 for PRIVILEGE_UNLOCK ");
        privilegeInt = Integer.valueOf(inputReader.readLine());;
     
       lockAccessParams.setLockPrivilege(getLockDataField(dataFieldInt), getPrivilege(privilegeInt));

	}
	
	private LOCK_PRIVILEGE getPrivilege(int privilege)
    {
        LOCK_PRIVILEGE lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_NONE;
        switch(privilege)
        {
            //Read-Write
            case 0:
                lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_READ_WRITE;
                break;
            //Permanent Lock
            case 1:
                lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_PERMA_LOCK;
                break;
            //Permanent unlock
            case 2:
                lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_PERMA_UNLOCK;
                break;
            //Unlock
            case 3:
                lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_UNLOCK;
                break;
        }

        return lockPrivilege;
    }
	private LOCK_DATA_FIELD getLockDataField(int dataField)
    {
        LOCK_DATA_FIELD lockDataField =LOCK_DATA_FIELD.LOCK_EPC_MEMORY;
        switch (dataField)
        {
            //Kill Password
            case 0:
            	lockDataField = LOCK_DATA_FIELD.LOCK_KILL_PASSWORD;
                break;
            //Access Password
            case 1:
            	lockDataField = LOCK_DATA_FIELD.LOCK_ACCESS_PASSWORD;
                break;
            //EPC Memory
            case 2:
            	lockDataField = LOCK_DATA_FIELD.LOCK_EPC_MEMORY;
                break;
            //TID Memory
            case 3:
            	lockDataField = LOCK_DATA_FIELD.LOCK_TID_MEMORY;
                break;  
            //User Memory
            case 4:
            	lockDataField = LOCK_DATA_FIELD.LOCK_USER_MEMORY;
                break;
        }

        return lockDataField;
    }

	
	private void getKillAccessParams ()throws NumberFormatException,IOException
	{
		 System.out.println("Enter KillPassword");
         killAccessParams.setKillPassword(Long.parseLong(inputReader.readLine(),16));
		
	}
	private void AccessOperationWithAccessFilter()
	{
		
		   Integer option;
           Boolean keepworking = true;
          
                   
           while(keepworking)
           {
        	 try
        	 {     
        		   addAccessFilter();
        		   
        			   
        		   System.out.println("");
                   System.out.println("----Command Menu----");
                   System.out.println("1.Read Tags");
                   System.out.println("2.Write Tags");
                   System.out.println("3.Lock Tags");
                   System.out.println("4.Kill Tags");
                   System.out.println("5.Go back to Access Menu");
                   option = Integer.valueOf(inputReader.readLine());
                   
                   accessEventLock.lock();
                   try
                   {
                   accessComplete = false;
                                    
                   switch (option)
                   {
                       case 1:
                     	   getReadAccessParams();
                     	   
                     	   myReader.Actions.TagAccess.readEvent(this.readAccessParams,this.accessFilter,null);
                           break;
                       case 2:
                           getWriteAccessParams();
                           myReader.Actions.TagAccess.writeEvent(this.writeAccessParams,this.accessFilter, null);
                           break;
                       case 3:
                           getLockAccessParams();
                           myReader.Actions.TagAccess.lockEvent(this.lockAccessParams,this.accessFilter,null);
                           break;
                       case 4:
                           getKillAccessParams();
                           myReader.Actions.TagAccess.killEvent(this.killAccessParams,this.accessFilter,null);
                           break;
                       default:
                           keepworking = false;
                           break;
                   }
                   
                   if(keepworking && !accessComplete)
                    {
                            try
                            {
                              accessEventCondVar.await();
                              int [] successCount = new int[1];
                              int [] failureCount = new int[1];
                              myReader.Actions.TagAccess.getLastAccessResult(successCount, failureCount);
                              System.out.println("Number of successes: "+successCount[0]+" Number of failures: "+failureCount[0]);
                              updateTags(true);
                            }
                            catch (InterruptedException e) { 

                            }
                    }
                   }
                   finally
                   {
                      accessEventLock.unlock();
                   }
                   
            	   
             }
    	     catch(NumberFormatException nfe)
             { 
             	 System.out.println("Invalid format"+nfe.getMessage());
             }
           
             catch(InvalidUsageException iue)
             {
             	  System.out.println("Invalid usage"+iue.getMessage());
             }
             catch (OperationFailureException opex)
             {
                 System.out.println("Access failed. Reason: " + opex.getVendorMessage());
             }
             catch(IOException ioe)
             {
             	  System.out.println("IO Exception"+ioe.getMessage());
             }
        	 catch(InputMismatchException iMe)
        	 {
        		 System.out.println(iMe.getMessage());
        	 }
        	
            catch (Exception ex)
            {
                 System.out.println(ex.toString());
            }
        
        	   
        	   
           }
         
          
		
	}
	
	private static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
	
	private void addAccessFilter() throws NumberFormatException,IOException,InputMismatchException
	{
		Integer memoryBank;
        String tagPattern,tagMask;
       
        String temp;
        
             
        System.out.println("New Access Filter or Remove filter?...Y/N/R");
        char input = inputReader.readLine().charAt(0);
        if(Character.toUpperCase(input) == 'Y')
        {
               System.out.println("---Access Filter Info---");
             System.out.println("Enter memoryBank");
             System.out.println("0 for RESERVED ");
             System.out.println("1 for EPC ");
             System.out.println("2 for TID ");
             System.out.println("3 for USER ");
        	
             accessFilter = new AccessFilter();
             accessFilter.setAccessFilterMatchPattern(FILTER_MATCH_PATTERN.A);
             accessFilter.TagPatternA = new TagPatternBase();
             accessFilter.TagPatternB = null;

             accessFilter.TagPatternA.setMemoryBank(MEMORY_BANK.GetMemoryBankValue(Integer.valueOf(inputReader.readLine())));
            
        	
            System.out.println("Enter offset in Bits ");
            accessFilter.TagPatternA.setBitOffset(Integer.valueOf(inputReader.readLine()));

            System.out.println("Enter Tag Pattern Length in Bits ");
            accessFilter.TagPatternA.setTagPatternBitCount(Integer.valueOf(inputReader.readLine()));
            
            System.out.println("Enter Tag Pattern ");
            tagPattern = inputReader.readLine();
            accessFilter.TagPatternA.setTagPattern(hexStringToByteArray(tagPattern));
                      
                          
            System.out.println("Enter tagMaskLength in Bits");
            accessFilter.TagPatternA.setTagMaskBitCount(Integer.valueOf(inputReader.readLine()));
           
            System.out.println("Enter tagMask ");
            tagMask = inputReader.readLine();
            accessFilter.TagPatternA.setTagMask(hexStringToByteArray(tagMask));
          
            System.out.println("Access Filter Set");
        	
        }
        else if(Character.toUpperCase(input) == 'R')
        {
        	this.accessFilter.TagPatternA = null;
                this.accessFilter.TagPatternB = null;
                this.accessFilter = null;
        }
        else if (Character.toUpperCase(input) != 'N')
        	throw new InputMismatchException("Y/N?R only allowed");
       
        	
              
        
	}
	
	private void InventoryMenu()
	{
		Integer option = 0;
        Boolean keepworking = true;
        while (keepworking)
        {
            System.out.println("");
            System.out.println("----Inventory Sub Menu----");
            System.out.println("1. Simple");
            System.out.println("2. Periodic Inventory");
            System.out.println("3. Pre-Filter");
            System.out.println("4. Back to Main Menu");
            
            try
            {

                option = Integer.valueOf(inputReader.readLine());

	            switch (option)
	            {
	                case 1:
	                    SimpleInventory();
                           
	                    break;
	                case 2:
	                    PeriodicInventory();
	                    break;
	                case 3:
	                    InventoryFilterOption();
	                    break;
	                default:
	                    keepworking = false;
	                    break;
            	}
            }
            catch(NumberFormatException nfe)
            { 
            	 System.out.println("Invalid format"+nfe.getMessage());
            }
    	    catch(IOException ioe)
            {
            	  System.out.println("IO Exception"+ioe.getMessage());
            }
            catch(InterruptedException ie)
    		{
    			System.out.println("Inventory interruped prematurely."+ie.getMessage());
    			
    		}
            catch (InvalidUsageException iue)
    		{
    		    System.out.println("Invalid usage.Reason: "+iue.getMessage());
    		}
    		catch(OperationFailureException opex)
    		{
    			System.out.println("Failed to start inventory.Reason: "+opex.getMessage());
    			
    		}
    		
        }
		  
	}
	
	private void SimpleInventory() throws InterruptedException,InvalidUsageException,OperationFailureException
	{
			
		   tagStore.clear();
                   
                  			
		   myReader.Actions.Inventory.perform();
                   
                   System.out.println("simple inventory started");
                   
                   System.out.println("Press Enter to stop inventory");
                   
                   try
                   { 
                      inputReader.readLine();
                   }
                   catch(IOException ioex)
                   {
                       System.out.println("IO Exception.Stopping inventory");
                   }
	       	   finally
                   {
                       myReader.Actions.Inventory.stop();
                      
                   }
                   
                   try
                   {
                       inventoryStopEventLock.lock();
                       if(!inventoryComplete)
                       {
                        inventoryStopCondVar.await();
                        inventoryComplete = false;
                       }
                       
                   }
		   finally
                   {
                       inventoryStopEventLock.unlock();
                   }
	 }
	
	private void PeriodicInventory() throws InterruptedException,InvalidUsageException,OperationFailureException
	{
		System.out.println("Periodic inventory started");
			
		try
		{  
			tagStore.clear();
			
			this.triggerInfo = new TriggerInfo();
			
			triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC);
	        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_DURATION);
	        
	        SYSTEMTIME startTime = new SYSTEMTIME();
	        Calendar  calendar = Calendar.getInstance();
	       	       
	        startTime.Year=(short)calendar.get(Calendar.YEAR);
	        startTime.Month=(short)calendar.get(Calendar.MONTH);      
	        startTime.Day=(short)calendar.get(Calendar.DAY_OF_MONTH);
            startTime.Hour=(short)calendar.get(Calendar.HOUR_OF_DAY);      
	        startTime.Minute=(short)calendar.get(Calendar.MINUTE);       
	        startTime.Second=(short)calendar.get(Calendar.SECOND);  
	        startTime.Second += 3;     
	        startTime.Milliseconds=0;
	        
	        triggerInfo.StartTrigger.Periodic.setPeriod(2000);
	        triggerInfo.StartTrigger.Periodic.StartTime = startTime;
	        triggerInfo.StopTrigger.setDurationMilliSeconds(1000);
	        triggerInfo.setTagReportTrigger(1);
	        
	        myReader.Actions.Inventory.perform(null,triggerInfo,null);
	        Thread.sleep(6000);
	        myReader.Actions.Inventory.stop();
	        
	        this.triggerInfo = new TriggerInfo();
	        this.triggerInfo.setTagReportTrigger(1);
			
		}
		finally
		{
			 myReader.Actions.Inventory.stop();
		}
		
        
	}
	
	
	
	private void InventoryFilterOption()
	{
		   Integer option = 0;
           Boolean keepworking = true;
           while (keepworking)
           {

               System.out.println("");
               System.out.println("----Command Menu----");
               System.out.println("1. Add Pre-Filter [only 1 filter is allowed]");
               System.out.println("2. Remove PreFilter");
               System.out.println("3. Back to Inventory Menu");
               try
               {
	               option = Integer.valueOf(inputReader.readLine());
	               switch (option)
	               {
	                   case 1:
	                       AddPreFilter();
	                       break;
	                   case 2:
	                       RemovePrefilter();
	                       break;
	                   case 3:
	                       keepworking = false;
	                       break;
	               }
               }
               catch(IOException ioe)
               {
               	  System.out.println("IO Exception"+ioe.getMessage());
               }
               catch(NumberFormatException nfe)
               { 
               	 System.out.println("Invalid format"+nfe.getMessage());
               }
               catch (InvalidUsageException iue)
       	       {
       	    	   System.out.println("Invalid usage.Reason: "+iue.getMessage());
       	       }
       	    
       	       catch (OperationFailureException opex)
               {
                   System.out.println("Add prefilter failed. Reason: " + opex.getVendorMessage());
               }
           }
	}
   
	private STATE_UNAWARE_ACTION getStateUnawareAction(Integer action) throws InvalidUsageException
	{  
		      
		switch(action)
		{
		case 0:
			return  STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_SELECT_NOT_UNSELECT;
			
		case 1:
			return  STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_SELECT;
			
		case 2:
			return STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_NOT_UNSELECT;
			
		case 3:
			return STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_UNSELECT;
			
		case 4:
			return STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_UNSELECT_NOT_SELECT;
			
		case 5:
			return STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_NOT_SELECT;
		default:
			throw new InvalidUsageException("InvalidUsageException","Valid range of StateUnawareaAction [0,5]");
			
		
		}
	
	}
	
	private void AddPreFilter() throws NumberFormatException,InvalidUsageException,IOException,OperationFailureException
	{
		
		String tagMask;
	    Integer memoryBank;
	    Integer action;
	    Long byteCount;
	    String temp;
	    
	    	   
	    System.out.println("----Command Menu----");
	    System.out.println("Enter AntennaID");
	    preFilter1.setAntennaID(Short.valueOf(inputReader.readLine()));
	 
	    
	    System.out.println("Enter Memorybank ");
	    System.out.println(" 1 for EPC  ");
	    System.out.println(" 2 for TID  ");
	    System.out.println(" 3 for USER  ");
	    preFilter1.setMemoryBank(MEMORY_BANK.GetMemoryBankValue(Integer.valueOf(inputReader.readLine())));
	    
	    System.out.println(" Enter Bit OffSet ");
	    preFilter1.setBitOffset(Integer.valueOf(inputReader.readLine()));
	    
	    
	    System.out.println(" Enter TagPattern");
	    preFilter1.setTagPattern(hexStringToByteArray(inputReader.readLine()));
	    
	    System.out.println(" Enter TagPattern's Bit count ");
	    preFilter1.setTagPatternBitCount(Integer.valueOf(inputReader.readLine()));
	    
	    preFilter1.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_UNAWARE);
	        
	    System.out.println("Enter stateUnawareAction");
	    System.out.println("0 for STATE_UNAWARE_ACTION_SELECT_NOT_UNSELECT ");
	    System.out.println("1 for STATE_UNAWARE_ACTION_SELECT ");
	    System.out.println("2 for STATE_UNAWARE_ACTION_NOT_UNSELECT ");
	    System.out.println("3 for STATE_UNAWARE_ACTION_UNSELECT ");
	    System.out.println("4 for STATE_UNAWARE_ACTION_UNSELECT_NOT_SELECT ");
	    System.out.println("5 for STATE_UNAWARE_ACTION_NOT_SELECT ");
	    
	    preFilter1.StateUnawareAction.setStateUnawareAction(getStateUnawareAction(Integer.valueOf(inputReader.readLine())));
	    
        myReader.Actions.PreFilters.deleteAll();
        
        myReader.Actions.PreFilters.add(preFilter1);
        System.out.println("Add PreFilter Successfully");
	   
	    
	   
       
	  	
	}
	
	private void RemovePrefilter() throws InvalidUsageException,OperationFailureException
	{
		 
         myReader.Actions.PreFilters.delete(preFilter1);
		 System.out.println("Remove PreFilter Successfully");
      
	}
    
	

	private void AccessMenu()
	{
		 Integer option = 0;
	       
	       Boolean keepworking = true;
         while (keepworking)
         {
             System.out.println("");
             System.out.println("----Access Sub Menu----");
             System.out.println("1. Access Operation with Specific EPC-ID ");
             System.out.println("2. Access Operation with Access-Filters");
             System.out.println("3. Back to Main Menu");
             
             try
             {
	               option = Integer.valueOf(inputReader.readLine());
	
	               switch (option)
	               {
	                   case 1:
	                	   
	           	           AccessOperationWithEPCID();
    	         	           
	                       break;
	                   case 2:
	                	   
	                       AccessOperationWithAccessFilter();
	                       
	                       break;
	                   case 3:
	                       keepworking = false;
	                       break;
	                   default:
	                	   System.out.println("Enter a valid integer in the range 1-3");
	                	   break;
	               }
             }
             catch (Exception ex)
             {
                 System.out.println(ex.toString());
             }
         }
	}
	


	public RFIDReader getMyReader() {
		return myReader;
	}
	
	void updateTags(Boolean isAccess)
	{
		
		myTags = myReader.Actions.getReadTags(50);
		if (myTags != null)
		{
				 if(!isAccess)
				 {
					 for (int index = 0; index < myTags.length; index++) 
					 {
						 TagData tag = myTags[index];
						 String key = tag.getTagID();
						// if (!tagStore.containsKey(key))
						// {
						//	tagStore.put(key,totalTags);
							postInfoMessage("ReadTag "+key); 
							//uniqueTags++;
						 // }
						 totalTags++;
					 }
				
				 }
				 else
				 {
					 for (int index = 0; index < myTags.length; index++)
					 {
						 TagData tag = myTags[index];
						 if(tag.getMemoryBankData() != null)
						    postInfoMessage("TagID "+tag.getTagID()+tag.getMemoryBank().toString()+"  "+tag.getMemoryBankData());
						 else
							 postInfoMessage("TagID "+tag.getTagID()+"Access Status:  "+tag.getOpStatus().toString()); 
						   	 
					 }
				 }
				
				
			
			
		}
		
	}
	
	void postStatusNotification(String statusMsg, String vendorMsg)
	{
		System.out.println("Status: "+statusMsg+" Vendor Message: "+vendorMsg);
	}
	
    static void postInfoMessage(String msg)
    {
    	System.out.println(msg);
    }
   
    public class EventsHandler implements RfidEventsListener
    {
    	public EventsHandler()
    	{
    		
    	}
    	
    	public void eventReadNotify(RfidReadEvents rre) {
			 
    		updateTags(false);
		}
    	
    	
    	
    	
    	public void eventStatusNotify(RfidStatusEvents rse)
    	{
    		postInfoMessage(rse.StatusEventData.getStatusEventType().toString());
    		
    		STATUS_EVENT_TYPE statusType = rse.StatusEventData.getStatusEventType();
    		if (statusType == STATUS_EVENT_TYPE.ACCESS_STOP_EVENT)
    		{
    			try
    			{
    			  accessEventLock.lock();
    			  accessComplete = true;
    			  accessEventCondVar.signalAll();
    			}
    			finally
    			{
    				accessEventLock.unlock();
    				
    			}
    			
    		}
                 else if(statusType == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT)
                 {
                     try
                     {
                         inventoryStopEventLock.lock();
                         inventoryComplete = true;
                         inventoryStopCondVar.signalAll();
                         
                     }
                     finally
                     {
                         inventoryStopEventLock.unlock();
                     }
                             
                 }
    		else if(statusType == STATUS_EVENT_TYPE.BUFFER_FULL_WARNING_EVENT || statusType == STATUS_EVENT_TYPE.BUFFER_FULL_EVENT)
    		{
    			postInfoMessage(statusType.toString());
    		}
    		
	   	}
    }
   
    	
	public boolean connectToReader(String readerHostName, int readerPort)
	{
		
		boolean retVal = false;
		hostName = readerHostName;
		port = readerPort;
		myReader.setHostName(hostName);
		myReader.setPort(port);
		
		try {
			myReader.connect();

			myReader.Events.setInventoryStartEvent(true);
			myReader.Events.setInventoryStopEvent(true);
			myReader.Events.setAccessStartEvent(true);
			myReader.Events.setAccessStopEvent(true);
			myReader.Events.setAntennaEvent(true);
			myReader.Events.setGPIEvent(true);
			myReader.Events.setBufferFullEvent(true);
			myReader.Events.setBufferFullWarningEvent(true);
			myReader.Events.setReaderDisconnectEvent(true);
			myReader.Events.setReaderExceptionEvent(true);
			myReader.Events.setTagReadEvent(true);
			myReader.Events.setAttachTagDataWithReadEvent(false);
                        
                        TagStorageSettings tagStorageSettings = myReader.Config.getTagStorageSettings();
                        tagStorageSettings.discardTagsOnInventoryStop(true);
                        myReader.Config.setTagStorageSettings(tagStorageSettings);

			myReader.Events.addEventsListener(eventsHandler);
			
			retVal = true;
			isConnected = true;
			postInfoMessage("Connected to " + hostName);
			postStatusNotification(API_SUCCESS, null);
			myReader.Config.setTraceLevel(TRACE_LEVEL.TRACE_LEVEL_ERROR);
			
			Createmenu();

		} catch (InvalidUsageException ex)
                {
		    postStatusNotification(PARAM_ERROR, ex.getVendorMessage());
		} catch (OperationFailureException ex) {
			postStatusNotification(ex.getStatusDescription(),
					ex.getVendorMessage());
		}
		
		
		return retVal;
		
	}
	
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		RFIDSample4App rfidBase; 
		rfidBase = new RFIDSample4App();
	}
	
  }
