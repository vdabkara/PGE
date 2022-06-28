CREATE TABLE TABLE_NAME_PLACEHOLDER (
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[VA_TYPE_ID] [varchar](100) NULL,
	[VA_DYNAMIC_ENTITY_ID] [varchar](100) NULL,
	[VA_LOCALE] [varchar](10) NULL,
	[VA_REG_CHANNEL] [varchar](200) NULL,
	[VA_MIGRATABLE_REFERENCE] [varchar](200) NULL,
	[VA_RM_TITLE] [varchar](500) NULL,
	[VA_RM_ENTITY_ID] [varchar](100) NULL,
	[VA_RM_TYPE_ID] [varchar](100) NULL,
	[REC_CREATION_TMSTP] [datetime] NULL,
	[REC_MODIFIED_TMSPT] [datetime] NULL,
	[KA_ARTICLE_ID] [varchar](100) NULL,
	[KA_PROCESSING_STATUS] [varchar](100) NULL,
	[KA_ERROR_MESSAGE] [varchar](4000) NULL,
	CONSTRAINT [PK_TABLE_NAME_PLACEHOLDER] PRIMARY KEY CLUSTERED 
  	(
		[ID] ASC
	)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]  	
