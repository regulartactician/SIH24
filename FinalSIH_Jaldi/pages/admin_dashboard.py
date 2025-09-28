import streamlit as st
import plotly.graph_objects as go
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

a,b,c = st.columns([0.7,0.15,0.15])
st.markdown('''<hr style="border:solid 2px #003FDD;margin: 0em;">''',unsafe_allow_html=True)
with a:
    st.title("Inventory Dashboard")
with b:
    if st.button("Home Page"):
        st.switch_page("pages/admin_all.py")
with c:
    if st.button("Logout"):
        st.session_state={}
        st.switch_page("admin_login.py")

a,b = st.columns([0.3,0.6])

with a:
    with st.container(border=True):
        st.subheader("FHTC Connections: ")
        st.markdown('''<hr style="border: solid 2px blue;">''',unsafe_allow_html=True)
        labels = ["Connections Exists (57)", "New Connection Requests (13)", "No Connections (30)"]
        values = [57, 13, 30]
        colors = ['#FF5733', '#003FDD', '#33FF57']

        # Create pie chart
        fig = go.Figure(data=[go.Pie(
            labels=labels,
            values=values,
            marker=dict(colors=colors),
            hole=0.4
        )])

        # Update layout
        fig.update_layout(
            title_text="FHTC Connections Distribution",
            annotations=[dict(
                text="Total", x=0.5, y=0.5, font_size=20, showarrow=False
            )]
        )

        # Display chart
        st.plotly_chart(fig, use_container_width=True)

with b:
    with st.container(border=True):
        st.subheader("Asset Information") 
        st.markdown('''<hr style="border: solid 2px blue;">''',unsafe_allow_html=True)
        opt = st.selectbox("Choose Resource Type: ", ["Reservoir", "WaterStorageTank"])
        if "voir" in opt:
            c,d,e = st.columns([0.25,0.25,0.5])
            with c:
                st.subheader("Water Level")
                data = 70  # percentage
                fig_water_level = go.Figure(go.Bar(
                    x=["Water Level"], y=[data],
                    marker_color='#003FDD',
                    text=f"{data}%",
                    textposition='auto'
                ))
                fig_water_level.update_layout(yaxis=dict(range=[0, 100]))
                st.plotly_chart(fig_water_level, use_container_width=True)

            with d:
                st.subheader("Water Quality")
                qdata = 55  # percentage
                fig_water_quality = go.Figure(go.Bar(
                    x=["Water Quality"], y=[qdata],
                    marker_color='#33FF57',
                    text=f"{qdata}%",
                    textposition='auto'
                ))
                fig_water_quality.update_layout(yaxis=dict(range=[0, 100]))
                st.plotly_chart(fig_water_quality, use_container_width=True)
        else:
            c,d,e = st.columns([0.25,0.25,0.5])
            with c:
                st.subheader("Water Level")
                data = 80  # percentage
                fig_water_level = go.Figure(go.Bar(
                    x=["Water Level"], y=[data],
                    marker_color='#003FDD',
                    text=f"{data}%",
                    textposition='auto'
                ))
                fig_water_level.update_layout(yaxis=dict(range=[0, 100]))
                st.plotly_chart(fig_water_level, use_container_width=True)

            with d:
                st.subheader("Water Quality")
                qdata = 55  # percentage
                fig_water_quality = go.Figure(go.Bar(
                    x=["Water Quality"], y=[qdata],
                    marker_color='#33FF57',
                    text=f"{qdata}%",
                    textposition='auto'
                ))
                fig_water_quality.update_layout(yaxis=dict(range=[0, 100]))
                st.plotly_chart(fig_water_quality, use_container_width=True)
