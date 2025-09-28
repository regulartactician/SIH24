import streamlit as st
import datetime
import firebase_admin
from firebase_admin import credentials, db
from fpdf import FPDF

st.set_page_config(layout="wide", initial_sidebar_state="collapsed")
st.write('<style>div.block-container{padding-top:0rem;}</style>', unsafe_allow_html=True)

# Apply custom styling
st.markdown("""
    <style>
    div.stButton > button:first-child {
        background-color: #003FDD;
        color: white;
        width: 200px;
        margin-top: 20px;
        text-align: center;
    }
    summary {
        background-color: #003FDD;
        font-size: 16px;
        font-weight: bold;
        border-radius: 20px;
        color: white;
        width: 200px;
        text-align: center;
    }
    .stVerticalBlockBorderWrapper {
        border: solid 2px #003FDD;
    }
    .st-emotion-cache-1puwf6r p {
        font-size: 18px;
        color: white;
    }
    </style>
""", unsafe_allow_html=True)

st.markdown('<hr style="border:solid 2px #003FDD;margin: 0em;">', unsafe_allow_html=True)



# Initialize Firebase
cred2 = credentials.Certificate("newjaldi-firebase-adminsdk-lpoc5-a2babade74.json")
try:
    firebase_admin.initialize_app(cred2, {
        'databaseURL': 'https://newjaldi-default-rtdb.firebaseio.com/'
    })
except Exception as e:
    pass


st.title("Asset Maintenance Scheduler")
st.markdown('<hr style="border:solid 2px #003FDD;margin: 0em;">', unsafe_allow_html=True)


class PDF(FPDF):
    def header(self):
        self.set_font("Arial",style="B", size=18)  
        self.image("logo_jaldi.png", 10, 8, 50)  
        self.cell(0, 10, "Maintenance Schedule Report", ln=True, align="C")
        self.ln(10)

    def footer(self):
        self.set_y(-15)
        self.cell(0, 10, f"Page {self.page_no()}", 0, 0, "C")


def fetch_all_assets():
    try:
        ref = db.reference("/")  
        assets = ref.get()
        return assets
    except Exception as e:
        st.error(f"Failed to fetch data from Firebase: {e}")
        return {}

# Function to calculate the next maintenance date
def calculate_next_maintenance(asset_type, last_maintenance_date):
    maintenance_intervals = {
        "Household": 180, "TapConnection": 90, "Reservoir": 30, "WaterTreatmentPlant": 15,
        "PumpingStation": 60, "Pipeline": 120, "WaterStorageTank": 180, "Sensor": 7, "AdministrativeData": 365
    }
    interval = maintenance_intervals.get(asset_type, 90)
    if last_maintenance_date:
        next_maintenance_date = last_maintenance_date + datetime.timedelta(days=interval)
    else:
        next_maintenance_date = datetime.datetime.now() + datetime.timedelta(days=interval)
    return next_maintenance_date

# Function to generate PDF report
def generate_pdf(maintenance_schedule):
    pdf = PDF()
    pdf.add_page()
    pdf.set_font("Arial", size=15)
    for schedule in maintenance_schedule:
        pdf.cell(0, 10, txt=f"{schedule['Asset Type']} - {schedule['Asset Name']}", ln=True)
        pdf.cell(0, 10, txt=f"  Last Maintenance Date: {schedule['Last Maintenance Date']}", ln=True)
        pdf.cell(0, 10, txt=f"  Next Maintenance Date: {schedule['Next Maintenance Date']}", ln=True)
        pdf.ln(5)

    return pdf

# Function to display and schedule maintenance
def schedule_maintenance():
    assets = fetch_all_assets()
    if not assets:
        st.info("No assets found in Firebase.")
        return

    maintenance_schedule = []

    for asset_type, asset_list in assets.items():
        if isinstance(asset_list, dict):
            for asset_id, asset_info in asset_list.items():
                last_maintenance_date = asset_info.get("lastMaintenanceDate")
                if last_maintenance_date:
                    last_maintenance_date = datetime.datetime.strptime(last_maintenance_date, "%Y-%m-%d")
                
                next_maintenance_date = calculate_next_maintenance(asset_type, last_maintenance_date)
                
                asset_name = asset_info.get("reservoirName", asset_id) if asset_type == "Reservoir" else asset_id

                maintenance_schedule.append({
                    "Asset Type": asset_type,
                    "Asset Name": asset_name,
                    "Last Maintenance Date": last_maintenance_date.strftime("%Y-%m-%d") if last_maintenance_date else "N/A",
                    "Next Maintenance Date": next_maintenance_date.strftime("%Y-%m-%d"),
                })

    # Display the maintenance schedule as cards
    st.header("Maintenance Schedule")
    col1, col2, col3 = st.columns(3)
    for i, schedule in enumerate(maintenance_schedule):
        with [col1, col2, col3][i % 3]:
            st.markdown(
                f"""
                <div style="border:1px solid #003FDD; border-radius:10px; padding:15px; margin-bottom:10px;">
                    <h4>{schedule['Asset Type']}</h4>
                    <p><strong>Name:</strong> {schedule['Asset Name']}</p>
                    <p><strong>Last Maintenance:</strong> {schedule['Last Maintenance Date']}</p>
                    <p><strong>Next Maintenance:</strong> {schedule['Next Maintenance Date']}</p>
                </div>
                """,
                unsafe_allow_html=True
            )

    if st.button("Save Schedule to Firebase"):
        try:
            ref = db.reference("MaintenanceSchedules")
            ref.set(maintenance_schedule)
            st.success("Maintenance schedule saved to Firebase successfully.")
        except Exception as e:
            st.error(f"Failed to save maintenance schedule: {e}")

    if st.button("Download Maintenance Report as PDF"):
        pdf = generate_pdf(maintenance_schedule)
        pdf_file = "Maintenance_Schedule_Report.pdf"
        pdf.output(pdf_file)

        with open(pdf_file, "rb") as file:
            st.download_button(
                label="Download PDF",
                data=file,
                file_name=pdf_file,
                mime="application/pdf"
            )

# Run the scheduler
schedule_maintenance()
