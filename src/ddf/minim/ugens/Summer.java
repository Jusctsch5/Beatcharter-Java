package ddf.minim.ugens;

import java.util.ArrayList;
import java.util.Arrays;

import ddf.minim.AudioSignal;
import ddf.minim.Minim;
import ddf.minim.UGen;

/**
 * A Summer allows you to sum the outputs of multiple UGens to be sent further
 * down the chain. Unlike most UGen effects, you can patch more than one UGen to
 * a Summer.
 * 
 * @example Synthesis/summerExample
 * 
 * @author Damien Di Fede
 * 
 */
public class Summer extends UGen implements AudioSignal
{
	private ArrayList<UGen>	m_ugens;
	private float[]			m_tickBuffer;

	/**
	 * Constructs a Summer that you can patch multiple UGens to.
	 * 
	 */
	public Summer()
	{
		m_ugens = new ArrayList<UGen>();
	}

	// ddf: override because everything that patches to us
	// goes into our list. then when we generate a sample
	// we'll sum the audio generated by all of the ugens patched to us.
	@Override
	protected void addInput(UGen input)
	{
		// Minim.debug( "Bus::addInput - Adding " + input + " to the m_ugens list of " + this );
		// it needs to know how many channels of audio we expect
		// we set the channel count before adding because concurrency means
		// that we might try to tick input between the add finishing and
		// setAudioChannelCount completing.
		input.setChannelCount( channelCount() );
		synchronized( m_ugens )
		{
			m_ugens.add( input );
		}
	}

	@Override
	protected void removeInput(UGen input)
	{
		Minim.debug( "Bus::removeInput - Removing " + input + " to the m_ugens list of " + this );
		synchronized( m_ugens )
		{
			for ( int i = 0; i < m_ugens.size(); ++i )
			{
				if ( m_ugens.get( i ) == input )
				{
					m_ugens.set( i, null );
				}
			}
		}
	}

	protected void sampleRateChanged()
	{
		// ddf: need to let all of the UGens in our list know about the sample rate change
		synchronized( m_ugens )
		{
			for ( int i = 0; i < m_ugens.size(); i++ )
			{
				UGen u = m_ugens.get( i );
				if ( u != null )
				{
					u.setSampleRate( sampleRate() );
				}
			}
		}
	}
	
	protected void channelCountChanged()
	{
		synchronized( m_ugens )
		{
			for( int i = 0; i < m_ugens.size(); ++i )
			{
				UGen u = m_ugens.get( i );
				if ( u != null )
				{
					u.setChannelCount( channelCount() );
				}
			}
		}
		
		m_tickBuffer = new float[ channelCount() ];
	}

	@Override
	protected void uGenerate(float[] channels)
	{
		// make sure we are generating the correct number of channels
		if ( m_tickBuffer == null || m_tickBuffer.length != channels.length )
		{
			m_tickBuffer = new float[channels.length];
			// and propagate that to our list
			synchronized( m_ugens )
			{
				for ( int i = 0; i < m_ugens.size(); ++i )
				{
					UGen u = m_ugens.get( i );
	
					if ( u != null )
					{
						u.setChannelCount( channels.length );
					}
					else
					// a null entry means it was unpatched, so go ahead and cull now
					{
						m_ugens.remove( i );
						--i;
					}
				}
			}
		}

		// start with silence
		Arrays.fill( channels, 0 );

		synchronized( m_ugens )
		{
			for ( int i = 0; i < m_ugens.size(); ++i )
			{
				// m_tickBuffer should be filled with the correct audio
				// even if this ugen has generated audio already
				UGen u = m_ugens.get( i );
	
				if ( u != null )
				{
					u.tick( m_tickBuffer );
					processSampleFrame( m_tickBuffer, channels );
				}
				else
				// a null entry means this ugen was unpatched, so we remove the
				// entry
				{
					m_ugens.remove( i );
					--i;
				}
			}
		}
	}

	// ddf: I broke this out into its own method so that Sink could extend Summer.
	// Doing this means not having to rewrite all of the UGen list handling
	// that Summer already does. The only difference between Summer and Sink
	// is that Sink produces silence.
	protected void processSampleFrame(float[] in, float[] out)
	{
		for ( int i = 0; i < out.length; ++i )
		{
			out[i] += in[i];
		}
	}

	/**
	 * Generates a buffer of samples by ticking this UGen mono.length times.
	 * Like the tick method, this will cause the entire UGen chain patched
	 * to this Summer to generate audio.
	 * 
	 * @example Advanced/OfflineRendering
	 */
	public void generate(float[] mono)
	{
		float[] sample = new float[1];
		for ( int i = 0; i < mono.length; i++ )
		{
			tick( sample );
			mono[i] = sample[0];
		}
	}

	public void generate(float[] left, float[] right)
	{
		float[] sample = new float[2];
		for ( int i = 0; i < left.length; i++ )
		{
			tick( sample );
			left[i] = sample[0];
			right[i] = sample[1];
		}
	}

}
