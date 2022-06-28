CREATE TABLE TAGS_VS_KA_REFERENCE (
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[VA_ITEM_NAME] [varchar](400) NULL,
	[ITEM_TYPE] [varchar](100) NULL,
	[KA_OSVC_ID] [varchar](50) NULL,
	[KA_REF_KEY] [varchar](200) NULL,
	[KA_RECORD_ID] [varchar](200) NULL,
	[KA_ITEM_NAME] [varchar](500) NULL,
	CONSTRAINT [PK_TAGS_VS_KA_REFERENCE] PRIMARY KEY CLUSTERED 
  	(
		[ID] ASC
	)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF,  IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]  	
